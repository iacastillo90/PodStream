package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.CommentDTO;
import com.podStream.PodStream.DTOS.CommentRequestDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar comentarios de clientes sobre productos en PodStream.
 */
@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comment Management", description = "APIs for managing client comments on products in the PodStream e-commerce platform")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(summary = "Create a new comment", description = "Creates a new comment for a product. Accessible to authenticated clients.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Comment created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(
            @Valid @RequestBody CommentRequestDTO request,
            Authentication authentication) {
        logger.info("Creating comment for client {} and product {}", request.getClientId(), request.getProductId());
        CommentDTO comment = commentService.createComment(request, authentication);
        return new ResponseEntity<>(ApiResponse.success("Comment created", comment), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by ID", description = "Retrieves a specific comment by its ID. Accessible to all authenticated users.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommentDTO>> getComment(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Fetching comment with id: {}", id);
        CommentDTO comment = commentService.getComment(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Comment retrieved", comment));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get comments by product", description = "Retrieves all comments for a specific product. Accessible to all authenticated users.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentsByProduct(
            @PathVariable @Positive(message = "Product ID must be positive") Long productId,
            Authentication authentication) {
        logger.info("Fetching comments for product: {}", productId);
        List<CommentDTO> comments = commentService.getCommentsByProduct(productId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved", comments));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get comments by client", description = "Retrieves all comments for a specific client. Accessible to the client or ADMIN.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and authentication.principal.id == #clientId)")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentsByClient(
            @PathVariable @Positive(message = "Client ID must be positive") Long clientId,
            Authentication authentication) {
        logger.info("Fetching comments for client: {}", clientId);
        List<CommentDTO> comments = commentService.getCommentsByClient(clientId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved", comments));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a comment", description = "Updates a specific comment. Accessible only to the comment's creator.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment, client, or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') and @commentServiceImpl.existsByIdAndClientId(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<CommentDTO>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequestDTO request,
            Authentication authentication) {
        logger.info("Updating comment with id: {}", id);
        CommentDTO updatedComment = commentService.updateComment(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Comment updated", updatedComment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment", description = "Soft deletes a specific comment. Accessible to ADMIN or the comment's creator.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Comment has associated answers"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @commentServiceImpl.existsByIdAndClientId(#id, authentication.principal.id))")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Deleting comment with id: {}", id);
        commentService.deleteComment(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }
}