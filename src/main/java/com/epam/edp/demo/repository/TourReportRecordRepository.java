package com.epam.edp.demo.repository;

import com.epam.edp.demo.model.TourReportRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourReportRecordRepository extends MongoRepository<TourReportRecord, String> {

    /** All tour rows for a specific reporting period, sorted by revenue descending */
    List<TourReportRecord> findByPeriodStartAndPeriodEndOrderByRevenueUsdDesc(
            LocalDate periodStart, LocalDate periodEnd);

    /** Used for upsert: find existing record for this tour + period */
    Optional<TourReportRecord> findByTourIdAndPeriodStartAndPeriodEnd(
            String tourId, LocalDate periodStart, LocalDate periodEnd);
}

