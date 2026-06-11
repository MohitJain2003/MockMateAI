package com.mockmate.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_questions")
public class InterviewQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long interviewId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    public InterviewQuestion() {}

    public InterviewQuestion(Long id, Long interviewId, String questionText) {
        this.id = id;
        this.interviewId = interviewId;
        this.questionText = questionText;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInterviewId() { return interviewId; }
    public void setInterviewId(Long interviewId) { this.interviewId = interviewId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
}
