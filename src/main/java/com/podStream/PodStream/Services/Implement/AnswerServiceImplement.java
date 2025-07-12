package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.AnswerDTO;
import com.podStream.PodStream.Models.Answers;
import com.podStream.PodStream.Models.Comment;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Repositories.Jpa.AnswerRepository;
import com.podStream.PodStream.Repositories.Elastic.ElasticAnswerRepository;
import com.podStream.PodStream.Repositories.Jpa.ClientRepository;
import com.podStream.PodStream.Repositories.Jpa.CommentRepository;
import com.podStream.PodStream.Services.AnswerService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AnswerServiceImplement implements AnswerService {

    private static final Logger logger = LoggerFactory.getLogger(AnswerServiceImplement.class);
    private static final String CACHE_PREFIX = "answer:";

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ElasticAnswerRepository elasticAnswerRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PodStreamPrometheusConfig podStreamPrometheusConfig;

    @Override
    @Transactional(readOnly = true)
    public List<AnswerDTO> findAll() {
        logger.info("Fetching all answers");
        return answerRepository.findAll()
                .stream()
                .map(AnswerDTO::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerDTO> findByCommentId(Long commentId) {
        logger.info("Fetching answers for comment id: {}", commentId);
        String cacheKey = CACHE_PREFIX + "comment:" + commentId;
        @SuppressWarnings("unchecked")
        List<AnswerDTO> cachedAnswers = (List<AnswerDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedAnswers != null) {
            logger.info("Returning cached answers for comment id: {}", commentId);
            return cachedAnswers;
        }
        List<AnswerDTO> answers = answerRepository.findByCommentId(commentId)
                .stream()
                .map(AnswerDTO::new)
                .toList();
        redisTemplate.opsForValue().set(cacheKey, answers, 1, TimeUnit.HOURS);
        return answers;
    }

    @Override
    @Transactional(readOnly = true)
    public AnswerDTO findById(Long id) {
        logger.info("Fetching answer with id: {}", id);
        String cacheKey = CACHE_PREFIX + id;
        AnswerDTO cachedAnswer = (AnswerDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedAnswer != null) {
            logger.info("Returning cached answer with id: {}", id);
            return cachedAnswer;
        }
        AnswerDTO answer = answerRepository.findById(id)
                .map(AnswerDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + id));
        redisTemplate.opsForValue().set(cacheKey, answer, 1, TimeUnit.HOURS);
        return answer;
    }

    @Override
    @Transactional
    public AnswerDTO createNewAnswer(AnswerDTO answerDTO, Authentication authentication) {
        logger.info("Creating new answer for comment id: {}", answerDTO.getCommentId());
        Long clientId = extractClientId(authentication);
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));
        Comment comment = commentRepository.findById(answerDTO.getCommentId())
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + answerDTO.getCommentId()));
        if (!comment.isActive()) {
            logger.warn("Cannot create answer for inactive comment id: {}", answerDTO.getCommentId());
            throw new IllegalStateException("Comment is inactive");
        }
        Answers answer = answerDTO.toEntity();
        answer.setClient(client);
        answer.setComment(comment);
        answer.setUserName(client.getUsername());
        Answers savedAnswer = answerRepository.save(answer);
        elasticAnswerRepository.save(savedAnswer);
        AnswerDTO savedAnswerDTO = new AnswerDTO(savedAnswer);
        redisTemplate.opsForValue().set(CACHE_PREFIX + savedAnswer.getId(), savedAnswerDTO, 1, TimeUnit.HOURS);
        redisTemplate.delete(CACHE_PREFIX + "comment:" + answerDTO.getCommentId());
        podStreamPrometheusConfig.incrementAnswerCreated();
        return savedAnswerDTO;
    }

    @Override
    @Transactional
    public AnswerDTO updateAnswer(Long id, AnswerDTO answerDTO, Authentication authentication) {
        logger.info("Updating answer with id: {}", id);
        Long clientId = extractClientId(authentication);
        Answers answer = answerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + id));
        if (!answerRepository.existsByIdAndClientId(id, clientId)) {
            logger.warn("Client id: {} not authorized to update answer id: {}", clientId, id);
            throw new SecurityException("Not authorized to update this answer");
        }
        Comment comment = commentRepository.findById(answerDTO.getCommentId())
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + answerDTO.getCommentId()));
        if (!comment.isActive()) {
            logger.warn("Cannot update answer for inactive comment id: {}", answerDTO.getCommentId());
            throw new IllegalStateException("Comment is inactive");
        }
        answer.setBody(answerDTO.getBody());
        answer.setComment(comment);
        Answers savedAnswer = answerRepository.save(answer);
        elasticAnswerRepository.save(savedAnswer);
        AnswerDTO savedAnswerDTO = new AnswerDTO(savedAnswer);
        redisTemplate.opsForValue().set(CACHE_PREFIX + id, savedAnswerDTO, 1, TimeUnit.HOURS);
        redisTemplate.delete(CACHE_PREFIX + "comment:" + answerDTO.getCommentId());
        podStreamPrometheusConfig.incrementAnswerUpdated();
        return savedAnswerDTO;
    }

    @Override
    @Transactional
    public void deleteById(Long id, Authentication authentication) {
        logger.info("Deleting answer with id: {}", id);
        Long clientId = extractClientId(authentication);
        if (!answerRepository.existsByIdAndClientId(id, clientId)) {
            logger.warn("Client id: {} not authorized to delete answer id: {}", clientId, id);
            throw new SecurityException("Not authorized to delete this answer");
        }
        Answers answer = answerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Answer not found with id: " + id));
        answer.setActive(false);
        answerRepository.save(answer);
        elasticAnswerRepository.save(answer);
        redisTemplate.delete(CACHE_PREFIX + id);
        redisTemplate.delete(CACHE_PREFIX + "comment:" + answer.getComment().getId());
        podStreamPrometheusConfig.incrementAnswerDeleted();
    }

    private Long extractClientId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        return Long.valueOf(authentication.getName());
    }
}
