package com.mockmate.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String jobRole;

    @Column(columnDefinition = "TEXT")
    private String techStack;

    private String experience;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String status = "PENDING"; // PENDING, COMPLETED

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    public Interview() {}

    public Interview(Long id, Long userId, String jobRole, String techStack, String experience, LocalDateTime createdAt, String status, Integer score, String feedback) {
        this.id = id;
        this.userId = userId;
        this.jobRole = jobRole;
        this.techStack = techStack;
        this.experience = experience;
        this.createdAt = createdAt;
        this.status = status;
        this.score = score;
        this.feedback = feedback;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }
    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
