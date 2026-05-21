package com.epam.edp.demo.repository;

import com.epam.edp.demo.model.BookingRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRecordRepository extends MongoRepository<BookingRecord, String> {

    List<BookingRecord> findByEventTypeAndEventTimestampBetween(
            String eventType, Instant from, Instant to);

    List<BookingRecord> findByEventTimestampBetween(Instant from, Instant to);
}

