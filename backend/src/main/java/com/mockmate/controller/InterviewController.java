package com.mockmate.controller;

import com.mockmate.entity.Interview;
import com.mockmate.entity.InterviewAnswer;
import com.mockmate.entity.InterviewQuestion;
import com.mockmate.repository.InterviewAnswerRepository;
import com.mockmate.repository.InterviewRepository;
import com.mockmate.repository.InterviewQuestionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private InterviewQuestionRepository interviewQuestionRepository;

    @Autowired
    private InterviewAnswerRepository interviewAnswerRepository;

    @Value("${google.search.api-key:}")
    private String apiKey;

    @Value("${google.search.cx:}")
    private String cx;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping
    public ResponseEntity<?> createInterview(@Valid @RequestBody CreateInterviewRequest request) {
        Interview interview = new Interview();
        interview.setUserId(request.getUserId());
        interview.setJobRole(request.getJobRole());
        interview.setTechStack(request.getTechStack());
        interview.setExperience(request.getExperience());
        interview.setCreatedAt(LocalDateTime.now());
        interview.setStatus("PENDING");

        Interview savedInterview = interviewRepository.save(interview);

        List<String> questionTexts = fetchQuestionsFromGoogle(request.getJobRole(), request.getTechStack(), request.getExperience());

        List<InterviewQuestion> savedQuestions = new ArrayList<>();
        for (String qText : questionTexts) {
            InterviewQuestion q = new InterviewQuestion();
            q.setInterviewId(savedInterview.getId());
            q.setQuestionText(qText);
            savedQuestions.add(interviewQuestionRepository.save(q));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("interviewId", savedInterview.getId());
        response.put("jobRole", savedInterview.getJobRole());
        response.put("techStack", savedInterview.getTechStack());
        response.put("experience", savedInterview.getExperience());
        response.put("questions", savedQuestions);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<?> getQuestions(@PathVariable Long id, @RequestParam Long userId) {
        Optional<Interview> interviewOpt = interviewRepository.findById(id);
        if (interviewOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!interviewOpt.get().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        List<InterviewQuestion> questions = interviewQuestionRepository.findByInterviewId(id);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitAnswers(@PathVariable Long id, @RequestParam Long userId, @Valid @RequestBody List<SubmitAnswerRequest> answersPayload) {
        Optional<Interview> interviewOpt = interviewRepository.findById(id);
        if (interviewOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!interviewOpt.get().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        Interview interview = interviewOpt.get();

        for (SubmitAnswerRequest req : answersPayload) {
            InterviewAnswer ans = new InterviewAnswer();
            ans.setInterviewId(id);
            ans.setQuestionId(req.getQuestionId());
            ans.setAnswerText(req.getAnswerText());
            interviewAnswerRepository.save(ans);
        }

        int score = calculateMockScore(answersPayload);
        String feedback = generateMockFeedback(interview, score);

        interview.setStatus("COMPLETED");
        interview.setScore(score);
        interview.setFeedback(feedback);
        interviewRepository.save(interview);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Interview completed and answers saved.");
        response.put("score", score);
        response.put("feedback", feedback);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getInterviewsByUser(@PathVariable Long userId) {
        List<Interview> interviews = interviewRepository.findByUserId(userId);
        return ResponseEntity.ok(interviews);
    }

    @SuppressWarnings("unchecked")
    private List<String> fetchQuestionsFromGoogle(String jobRole, String techStack, String experience) {
        List<String> questions = new ArrayList<>();
        try {
            if (apiKey != null && !apiKey.isEmpty() && cx != null && !cx.isEmpty()) {
                String searchQuery = String.format("%s %s %s interview questions", jobRole, techStack, experience);
                String url = String.format("https://www.googleapis.com/customsearch/v1?q=%s&key=%s&cx=%s",
                        URLEncoder.encode(searchQuery, StandardCharsets.UTF_8), apiKey, cx);

                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.containsKey("items")) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    for (Map<String, Object> item : items) {
                        if (item.containsKey("snippet")) {
                            String snippet = (String) item.get("snippet");
                            if (snippet != null && snippet.trim().length() > 20) {
                                questions.add(snippet.replaceAll("\\s+", " ").trim());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching questions from Google search: " + e.getMessage());
        }

        if (questions.isEmpty()) {
            questions.add("Can you explain the architecture of a typical application you've built using " + techStack + "?");
            questions.add("What are the most challenging problems you face when working with " + techStack + ", and how do you resolve them?");
            questions.add("How do you design a system to handle high traffic and ensure optimal performance for a " + jobRole + " position?");
            questions.add("What is your approach to testing, debugging, and maintaining code quality in a team environment?");
            questions.add("Can you describe a time when you had to learn a new technology quickly to solve a problem on the job?");
            questions.add("Explain the difference between synchronous and asynchronous programming, and how it applies to " + techStack + ".");
            questions.add("How do you manage security, authentication, and data protection in your applications?");
            questions.add("Describe a difficult conflict or technical disagreement you had with a team member and how you resolved it.");
            questions.add("How do you keep your technical skills updated with the latest trends and versions of " + techStack + "?");
            questions.add("Why do you think you are a good fit for this " + jobRole + " role, and what value can you bring to our team?");
        }

        return questions.subList(0, Math.min(questions.size(), 10));
    }

    private int calculateMockScore(List<SubmitAnswerRequest> answers) {
        if (answers == null || answers.isEmpty()) return 0;
        int totalWords = 0;
        int answeredCount = 0;
        for (SubmitAnswerRequest ans : answers) {
            if (ans.getAnswerText() != null && !ans.getAnswerText().trim().isEmpty()) {
                answeredCount++;
                totalWords += ans.getAnswerText().split("\\s+").length;
            }
        }
        if (answeredCount == 0) return 0;

        int averageWords = totalWords / answeredCount;
        int score = 50;
        if (averageWords > 20) score += 15;
        if (averageWords > 40) score += 20;
        if (answeredCount > 5) score += 15;

        return Math.min(score, 100);
    }

    private String generateMockFeedback(Interview interview, int score) {
        StringBuilder fb = new StringBuilder();
        fb.append(String.format("### Interview Feedback Report for %s Role\n\n", interview.getJobRole()));
        fb.append(String.format("**Overall Rating**: %d/100\n\n", score));
        if (score >= 80) {
            fb.append("Excellent communication skills and depth of tech stack knowledge shown. Answers were descriptive and covered key topics.");
        } else if (score >= 60) {
            fb.append("Good response quality, but answers could be more detailed. Focus on providing real-world examples and project achievements.");
        } else {
            fb.append("Need improvement in elaborating answers. Try to structure responses using the STAR method (Situation, Task, Action, Result) and use more industry-standard tech keywords.");
        }
        fb.append("\n\n**Recommended Focus Areas**:\n");
        fb.append(String.format("- Practice coding/concept questions specifically on: %s.\n", interview.getTechStack()));
        fb.append("- Work on mock speaking speed and fluency to avoid short answers.");
        return fb.toString();
    }

    public static class CreateInterviewRequest {
        @NotNull(message = "User ID is required")
        private Long userId;

        @NotBlank(message = "Job role is required")
        @Size(max = 200, message = "Job role must not exceed 200 characters")
        private String jobRole;

        @Size(max = 500, message = "Tech stack must not exceed 500 characters")
        private String techStack;

        @Size(max = 100, message = "Experience must not exceed 100 characters")
        private String experience;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getJobRole() { return jobRole; }
        public void setJobRole(String jobRole) { this.jobRole = jobRole; }
        public String getTechStack() { return techStack; }
        public void setTechStack(String techStack) { this.techStack = techStack; }
        public String getExperience() { return experience; }
        public void setExperience(String experience) { this.experience = experience; }
    }

    public static class SubmitAnswerRequest {
        @NotNull(message = "Question ID is required")
        private Long questionId;

        @Size(max = 5000, message = "Answer must not exceed 5000 characters")
        private String answerText;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getAnswerText() { return answerText; }
        public void setAnswerText(String answerText) { this.answerText = answerText; }
    }
}
