package com.podStream.PodStream.Controllers;

import com.podStream.PodStream.DTOS.AnswerDTO;
import com.podStream.PodStream.Models.ApiResponse;
import com.podStream.PodStream.Services.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answer")
@Tag(name = "Answer Management", description = "APIs for managing answers in the PodStream e-commerce platform")
public class AnswerController {

    private static final Logger logger = LoggerFactory.getLogger(AnswerController.class);

    @Autowired
    private AnswerService answerService;

    @GetMapping
    @Operation(summary = "List all answers", description = "Retrieves a list of all answers. Accessible only to ADMIN users.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Answers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnswerDTO>>> getAllAnswers() {
        logger.info("Fetching all answers");
        return ResponseEntity.ok(ApiResponse.success("Answers retrieved", answerService.findAll()));
    }

    @GetMapping("/comment/{commentId}")
    @Operation(summary = "List answers by comment ID", description = "Retrieves all answers for a specific comment. Accessible to all authenticated users.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Answers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AnswerDTO>>> getAnswersByCommentId(@PathVariable Long commentId) {
        logger.info("Fetching answers for comment id: {}", commentId);
        return ResponseEntity.ok(ApiResponse.success("Answers retrieved", answerService.findByCommentId(commentId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an answer by ID", description = "Retrieves an answer's details by its ID. Accessible to ADMIN or the answer's creator.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Answer retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Answer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @answerServiceImpl.existsByIdAndClientId(#id, authentication.principal.id))")
    public ResponseEntity<ApiResponse<AnswerDTO>> getAnswerById(@PathVariable Long id) {
        logger.info("Fetching answer with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Answer retrieved", answerService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new answer", description = "Creates a new answer for a comment. The client is obtained from the authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Answer created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment or client not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Comment is inactive"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<AnswerDTO>> createNewAnswer(@Valid @RequestBody AnswerDTO answerDTO, Authentication authentication) {
        logger.info("Creating answer for comment id: {}", answerDTO.getCommentId());
        return ResponseEntity.ok(ApiResponse.success("Answer created", answerService.createNewAnswer(answerDTO, authentication)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an answer", description = "Updates an existing answer. Accessible only to the answer's creator.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Answer updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Answer or comment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Comment is inactive"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('CLIENT') and @answerServiceImpl.existsByIdAndClientId(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<AnswerDTO>> updateAnswer(@PathVariable Long id, @Valid @RequestBody AnswerDTO answerDTO, Authentication authentication) {
        logger.info("Updating answer with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Answer updated", answerService.updateAnswer(id, answerDTO, authentication)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an answer", description = "Soft deletes an answer by setting active to false. Accessible to ADMIN or the answer's creator.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Answer deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Answer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and @answerServiceImpl.existsByIdAndClientId(#id, authentication.principal.id))")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(@PathVariable Long id, Authentication authentication) {
        logger.info("Deleting answer with id: {}", id);
        answerService.deleteById(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Answer deleted", null));
    }
}