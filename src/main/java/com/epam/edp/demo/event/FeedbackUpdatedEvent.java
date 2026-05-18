package com.epam.edp.demo.event;

public class FeedbackUpdatedEvent {

    private final String feedbackId;
    private final String bookingId;
    private final String tourId;
    private final String customerId;

    public FeedbackUpdatedEvent(String feedbackId, String bookingId, String tourId, String customerId) {
        this.feedbackId = feedbackId;
        this.bookingId = bookingId;
        this.tourId = tourId;
        this.customerId = customerId;
    }

    public String getFeedbackId() { return feedbackId; }
    public String getBookingId()  { return bookingId; }
    public String getTourId()     { return tourId; }
    public String getCustomerId() { return customerId; }
}
