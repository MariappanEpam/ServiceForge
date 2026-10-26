package com.serviceforge.controller;

import com.serviceforge.dto.FeedbackRequest;
import com.serviceforge.dto.FeedbackResponse;
import com.serviceforge.dto.FeedbackSummaryResponse;
import com.serviceforge.model.Feedback;
import com.serviceforge.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * ETSTCC-13074: feedback endpoints. Full-path mappings (no class-level mapping) so the summary URL stays
 * /api/technicians/{id}/feedback-summary. Privacy: never logs the request object; consent=true is
 * enforced by FeedbackRequest validation before the service is called.
 */
@RestController
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping("/api/feedback")
    public ResponseEntity<FeedbackResponse> submit(@Valid @RequestBody FeedbackRequest request) {
        Feedback saved = service.submit(request.getJobId(), request.getRating(), request.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).body(FeedbackResponse.from(saved));
    }

    @GetMapping("/api/technicians/{id}/feedback-summary")
    public ResponseEntity<FeedbackSummaryResponse> summary(@PathVariable Long id) {
        return ResponseEntity.ok(service.summarize(id));
    }
}
