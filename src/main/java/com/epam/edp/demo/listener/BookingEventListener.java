package com.epam.edp.demo.listener;

import com.epam.edp.demo.dto.BookingEvent;
import com.epam.edp.demo.model.BookingRecord;
import com.epam.edp.demo.repository.BookingRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Consumes BookingEvent messages from RabbitMQ and persists them
 * as BookingRecord documents for weekly aggregation.
 */
@Component
public class BookingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    private final BookingRecordRepository repository;

    public BookingEventListener(BookingRecordRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue:booking.events.queue}")
    public void onBookingEvent(BookingEvent event) {
        log.info("Received {} event for bookingId={}", event.getEventType(), event.getBookingId());

        BookingRecord record = new BookingRecord();
        record.setEventType(event.getEventType());
        record.setBookingId(event.getBookingId());
        record.setTourId(event.getTourId());
        record.setTourName(event.getTourName());
        record.setDestination(event.getDestination());
        record.setAgentId(event.getAgentId());
        record.setAgentName(event.getAgentName());
        record.setAgentEmail(event.getAgentEmail());
        record.setUserId(event.getUserId());
        record.setAdults(event.getAdults());
        record.setChildren(event.getChildren());
        record.setRevenueAmount(event.getRevenueAmount());
        record.setTourRating(event.getTourRating());
        record.setTourReviewCount(event.getTourReviewCount());
        record.setDuration(event.getDuration());
        record.setEventTimestamp(event.getTimestamp());

        if (event.getBookingDate() != null) {
            try {
                record.setBookingDate(LocalDate.parse(event.getBookingDate()));
            } catch (Exception e) {
                log.warn("Could not parse bookingDate '{}': {}", event.getBookingDate(), e.getMessage());
            }
        }

        repository.save(record);
        log.debug("Saved BookingRecord id={} for bookingId={}", record.getId(), event.getBookingId());
    }
}

