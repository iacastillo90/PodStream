package com.podStream.PodStream.Services;

import com.podStream.PodStream.DTOS.CommentDTO;
import com.podStream.PodStream.DTOS.CommentRequestDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface CommentService {
    CommentDTO createComment(CommentRequestDTO request, Authentication authentication);
    CommentDTO getComment(Long id, Authentication authentication);
    List<CommentDTO> getCommentsByProduct(Long productId, Authentication authentication);
    List<CommentDTO> getCommentsByClient(Long clientId, Authentication authentication);
    CommentDTO updateComment(Long id, CommentRequestDTO request, Authentication authentication);
    void deleteComment(Long id, Authentication authentication);
}
