package com.podStream.PodStream.Services.Implement;

import com.podStream.PodStream.Configurations.PodStreamPrometheusConfig;
import com.podStream.PodStream.DTOS.CommentDTO;
import com.podStream.PodStream.DTOS.CommentRequestDTO;
import com.podStream.PodStream.Models.Comment;
import com.podStream.PodStream.Models.User.Client;
import com.podStream.PodStream.Models.Product;
import com.podStream.PodStream.Repositories.Jpa.CommentRepository;
import com.podStream.PodStream.Repositories.Elastic.CommentElasticRepository;
import com.podStream.PodStream.Repositories.Jpa.ClientRepository;
import com.podStream.PodStream.Repositories.Jpa.ProductRepository;
import com.podStream.PodStream.Services.CommentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CommentServiceImplement implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImplement.class);
    private static final String COMMENT_KEY_PREFIX = "comment:active:";
    private static final long COMMENT_TTL_MINUTES = 1440; // 1 día para comentarios

    private final CommentRepository commentRepository;
    private final CommentElasticRepository searchRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PodStreamPrometheusConfig podStreamPrometheusConfig;

    public CommentServiceImplement(
            CommentRepository commentRepository,
            CommentElasticRepository searchRepository,
            ClientRepository clientRepository,
            ProductRepository productRepository,
            RedisTemplate<String, Object> redisTemplate,
            PodStreamPrometheusConfig podStreamPrometheusConfig) {
        this.commentRepository = commentRepository;
        this.searchRepository = searchRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.podStreamPrometheusConfig = podStreamPrometheusConfig;
    }

    @Override
    @Transactional
    public CommentDTO createComment(CommentRequestDTO request, Authentication authentication) {
        logger.info("Creating comment for client {} and product {}", request.getClientId(), request.getProductId());
        Long clientId = validateAuthentication(authentication);
        validateClientOwnership(clientId, request.getClientId());

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + request.getClientId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductId()));

        Comment comment = new Comment();
        comment.setBody(request.getBody());
        comment.setClient(client);
        comment.setProduct(product);
        comment.setDate(LocalDateTime.now());
        comment.setActive(true);

        Comment savedComment = commentRepository.save(comment);
        searchRepository.save(savedComment);
        redisTemplate.opsForValue().set(COMMENT_KEY_PREFIX + savedComment.getId(), savedComment, COMMENT_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementCommentCreated();
        return new CommentDTO(savedComment);
    }

    @Override
    public CommentDTO getComment(Long id, Authentication authentication) {
        logger.info("Fetching comment with id: {}", id);
        validateAuthentication(authentication);

        String cacheKey = COMMENT_KEY_PREFIX + id;
        Comment cachedComment = (Comment) redisTemplate.opsForValue().get(cacheKey);
        if (cachedComment != null && cachedComment.isActive()) {
            logger.info("Comment id: {} retrieved from cache", id);
            podStreamPrometheusConfig.incrementCommentCacheHit();
            return new CommentDTO(cachedComment);
        }

        Comment comment = commentRepository.findById(id)
                .filter(Comment::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));

        redisTemplate.opsForValue().set(cacheKey, comment, COMMENT_TTL_MINUTES, TimeUnit.MINUTES);
        podStreamPrometheusConfig.incrementCommentFetched();
        return new CommentDTO(comment);
    }

    @Override
    public List<CommentDTO> getCommentsByProduct(Long productId, Authentication authentication) {
        logger.info("Fetching comments for product: {}", productId);
        validateAuthentication(authentication);

        List<Comment> comments = commentRepository.findByProductIdAndActiveTrue(productId);
        podStreamPrometheusConfig.incrementCommentFetched();
        return comments.stream()
                .map(comment -> {
                    redisTemplate.opsForValue().set(COMMENT_KEY_PREFIX + comment.getId(), comment, COMMENT_TTL_MINUTES, TimeUnit.MINUTES);
                    return new CommentDTO(comment);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDTO> getCommentsByClient(Long clientId, Authentication authentication) {
        logger.info("Fetching comments for client: {}", clientId);
        Long authenticatedClientId = validateAuthentication(authentication);
        validateClientOwnership(authenticatedClientId, clientId);

        List<Comment> comments = commentRepository.findByClientIdAndActiveTrue(clientId);
        podStreamPrometheusConfig.incrementCommentFetched();
        return comments.stream()
                .map(comment -> {
                    redisTemplate.opsForValue().set(COMMENT_KEY_PREFIX + comment.getId(), comment, COMMENT_TTL_MINUTES, TimeUnit.MINUTES);
                    return new CommentDTO(comment);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDTO updateComment(Long id, CommentRequestDTO request, Authentication authentication) {
        logger.info("Updating comment with id: {}", id);
        Long clientId = validateAuthentication(authentication);

        Comment comment = commentRepository.findById(id)
                .filter(Comment::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));
        validateClientOwnership(clientId, comment.getClient().getId());

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + request.getClientId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.getProductId()));

        comment.setBody(request.getBody());
        comment.setClient(client);
        comment.setProduct(product);

        Comment updatedComment = commentRepository.save(comment);
        searchRepository.save(updatedComment);
        redisTemplate.opsForValue().set(COMMENT_KEY_PREFIX + updatedComment.getId(), updatedComment, COMMENT_TTL_MINUTES, TimeUnit.MINUTES);

        podStreamPrometheusConfig.incrementCommentUpdated();
        return new CommentDTO(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Authentication authentication) {
        logger.info("Deleting comment with id: {}", id);
        Long clientId = validateAuthentication(authentication);

        Comment comment = commentRepository.findById(id)
                .filter(Comment::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));
        validateClientOwnership(clientId, comment.getClient().getId());

        if (!comment.getAnswers().isEmpty()) {
            podStreamPrometheusConfig.incrementCommentErrors();
            throw new IllegalStateException("Cannot delete comment with associated answers");
        }

        comment.setActive(false);
        commentRepository.save(comment);
        searchRepository.save(comment);
        redisTemplate.delete(COMMENT_KEY_PREFIX + id);

        podStreamPrometheusConfig.incrementCommentDeleted();
    }

    private Long validateAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Unauthorized access attempt");
            podStreamPrometheusConfig.incrementCommentErrors();
            throw new SecurityException("Authentication required");
        }
        return Long.valueOf(authentication.getName());
    }

    private void validateClientOwnership(Long authenticatedClientId, Long requestedClientId) {
        Optional<Client> authentication  = clientRepository.findById(authenticatedClientId);
        if (!authenticatedClientId.equals(requestedClientId) && !authenticationHasRole(authentication, "ADMIN")) {
            logger.warn("Client id: {} not authorized to access comments for client: {}", authenticatedClientId, requestedClientId);
            podStreamPrometheusConfig.incrementCommentErrors();
            throw new SecurityException("Not authorized to access this client's comments");
        }
    }

    private boolean authenticationHasRole(Optional<Client> authentication, String admin) {
        return false;
    }

    private boolean authenticationHasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}