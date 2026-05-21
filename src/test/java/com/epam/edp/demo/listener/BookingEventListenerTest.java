package com.epam.edp.demo.listener;

import com.epam.edp.demo.dto.BookingEvent;
import com.epam.edp.demo.model.BookingRecord;
import com.epam.edp.demo.repository.BookingRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingEventListenerTest {

    @Mock BookingRecordRepository repository;
    @InjectMocks BookingEventListener listener;

    @Test
    void onBookingEvent_mapsAllFieldsAndSaves() {
        Instant now = Instant.now();
        BookingEvent event = BookingEvent.builder()
                .eventType("BOOKING_CREATED")
                .bookingId("bk-001")
                .tourId("tour-001")
                .tourName("Tropical Caribe")
                .destination("Dominican Republic")
                .agentId("agent-01")
                .agentName("Alice")
                .agentEmail("alice@test.com")
                .userId("user-01")
                .adults(2)
                .children(1)
                .revenueAmount(3000L)
                .tourRating(4.5)
                .tourReviewCount(12)
                .bookingDate("2026-05-11")
                .duration("7 nights")
                .timestamp(now)
                .build();

        listener.onBookingEvent(event);

        ArgumentCaptor<BookingRecord> captor = ArgumentCaptor.forClass(BookingRecord.class);
        verify(repository).save(captor.capture());
        BookingRecord saved = captor.getValue();

        assertThat(saved.getEventType()).isEqualTo("BOOKING_CREATED");
        assertThat(saved.getBookingId()).isEqualTo("bk-001");
        assertThat(saved.getTourId()).isEqualTo("tour-001");
        assertThat(saved.getTourName()).isEqualTo("Tropical Caribe");
        assertThat(saved.getDestination()).isEqualTo("Dominican Republic");
        assertThat(saved.getAgentId()).isEqualTo("agent-01");
        assertThat(saved.getAgentName()).isEqualTo("Alice");
        assertThat(saved.getAgentEmail()).isEqualTo("alice@test.com");
        assertThat(saved.getAdults()).isEqualTo(2);
        assertThat(saved.getChildren()).isEqualTo(1);
        assertThat(saved.getRevenueAmount()).isEqualTo(3000L);
        assertThat(saved.getTourRating()).isEqualTo(4.5);
        assertThat(saved.getBookingDate()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(saved.getDuration()).isEqualTo("7 nights");
        assertThat(saved.getEventTimestamp()).isEqualTo(now);
    }

    @Test
    void onBookingEvent_nullBookingDate_stillSavesRecord() {
        BookingEvent event = BookingEvent.builder()
                .eventType("BOOKING_CANCELLED")
                .bookingId("bk-002")
                .timestamp(Instant.now())
                .build();

        listener.onBookingEvent(event);

        ArgumentCaptor<BookingRecord> captor = ArgumentCaptor.forClass(BookingRecord.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getBookingDate()).isNull();
    }

    @Test
    void onBookingEvent_invalidBookingDate_stillSavesRecordWithNullDate() {
        BookingEvent event = BookingEvent.builder()
                .eventType("BOOKING_CREATED")
                .bookingId("bk-003")
                .bookingDate("not-a-date")
                .timestamp(Instant.now())
                .build();

        listener.onBookingEvent(event);

        ArgumentCaptor<BookingRecord> captor = ArgumentCaptor.forClass(BookingRecord.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getBookingDate()).isNull();
    }
}

