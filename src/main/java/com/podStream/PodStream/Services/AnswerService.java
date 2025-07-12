package com.podStream.PodStream.Services;


import com.podStream.PodStream.DTOS.AnswerDTO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AnswerService {
    List<AnswerDTO> findAll();
    List<AnswerDTO> findByCommentId(Long commentId);
    AnswerDTO findById(Long id);
    AnswerDTO createNewAnswer(@Valid AnswerDTO answerDTO, Authentication authentication);
    AnswerDTO updateAnswer(Long id, @Valid AnswerDTO answerDTO, Authentication authentication);
    void deleteById(Long id, Authentication authentication);
}

