package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.Answers;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnswerDTO {

    private Long id;

    @NotBlank(message = "Body is required")
    @Size(max = 10000, message = "Body must not exceed 10000 characters")
    private String body;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String userName;

    @NotNull(message = "Comment ID is required")
    private Long commentId;

    private Long clientId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean active;

    public AnswerDTO() {}

    public AnswerDTO(Answers answer) {
        this.id = answer.getId();
        this.body = answer.getBody();
        this.userName = answer.getUserName();
        this.commentId = answer.getComment().getId();
        this.clientId = answer.getClient().getId();
        this.createdAt = answer.getCreatedAt();
        this.updatedAt = answer.getUpdatedAt();
        this.active = answer.isActive();
    }

    public Answers toEntity() {
        Answers answer = new Answers();
        answer.setId(this.id);
        answer.setBody(this.body);
        answer.setUserName(this.userName);
        answer.setActive(this.active);
        return answer;
    }
}
