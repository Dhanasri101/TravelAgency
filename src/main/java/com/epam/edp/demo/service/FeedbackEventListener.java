package com.epam.edp.demo.service;

import com.epam.edp.demo.dto.FeedbackSubmittedEvent;
import com.epam.edp.demo.dto.FeedbackUpdatedEvent;
import com.epam.edp.demo.enums.FeedbackStatus;
import com.epam.edp.demo.model.Feedback;
import com.epam.edp.demo.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for feedback domain events and runs asynchronous moderation.
 * Status transitions: PENDING → APPROVED or FLAGGED.
 */
@Component
public class FeedbackEventListener {

    private static final Logger log = LoggerFactory.getLogger(FeedbackEventListener.class);

    private final FeedbackRepository feedbackRepository;
    private final FeedbackService feedbackService;

    public FeedbackEventListener(FeedbackRepository feedbackRepository,
                                  FeedbackService feedbackService) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackService = feedbackService;
    }

    @Async
    @EventListener
    public void onFeedbackSubmitted(FeedbackSubmittedEvent event) {
        log.info("Moderating newly submitted feedback id={}", event.getFeedbackId());
        applyModeration(event.getFeedbackId(), event.getComment());
    }

    @Async
    @EventListener
    public void onFeedbackUpdated(FeedbackUpdatedEvent event) {
        log.info("Re-moderating updated feedback id={}", event.getFeedbackId());
        applyModeration(event.getFeedbackId(), event.getComment());
    }

    private void applyModeration(String feedbackId, String comment) {
        feedbackRepository.findById(feedbackId).ifPresentOrElse(feedback -> {
            FeedbackStatus result = feedbackService.moderateComment(comment);
            feedback.setStatus(result);
            feedbackRepository.save(feedback);
            log.info("Feedback id={} moderation result: {}", feedbackId, result);
        }, () -> log.warn("Feedback id={} not found during moderation — skipped", feedbackId));
    }
}
