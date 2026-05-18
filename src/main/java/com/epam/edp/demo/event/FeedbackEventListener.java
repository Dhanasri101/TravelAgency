package com.epam.edp.demo.event;

import com.epam.edp.demo.enums.FeedbackStatus;
import com.epam.edp.demo.model.Feedback;
import com.epam.edp.demo.repository.FeedbackRepository;
import com.epam.edp.demo.service.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FeedbackEventListener {

    private static final Logger log = LoggerFactory.getLogger(FeedbackEventListener.class);

    private final FeedbackRepository feedbackRepository;
    private final FeedbackService feedbackService;

    public FeedbackEventListener(FeedbackRepository feedbackRepository, FeedbackService feedbackService) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackService = feedbackService;
    }

    @EventListener
    public void handleFeedbackSubmitted(FeedbackSubmittedEvent event) {
        log.debug("Processing moderation for submitted feedback: {}", event.getFeedbackId());
        applyModeration(event.getFeedbackId());
    }

    @EventListener
    public void handleFeedbackUpdated(FeedbackUpdatedEvent event) {
        log.debug("Processing moderation for updated feedback: {}", event.getFeedbackId());
        applyModeration(event.getFeedbackId());
    }

    private void applyModeration(String feedbackId) {
        feedbackRepository.findById(feedbackId).ifPresent(feedback -> {
            FeedbackStatus newStatus = feedbackService.moderateComment(feedback.getComment());
            feedback.setStatus(newStatus);
            feedbackRepository.save(feedback);
            log.debug("Feedback {} moderation result: {}", feedbackId, newStatus);
        });
    }
}
