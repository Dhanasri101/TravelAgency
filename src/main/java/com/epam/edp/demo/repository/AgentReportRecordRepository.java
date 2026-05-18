package com.epam.edp.demo.repository;

import com.epam.edp.demo.model.AgentReportRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentReportRecordRepository extends MongoRepository<AgentReportRecord, String> {

    /** All agent rows for a specific reporting period */
    List<AgentReportRecord> findByPeriodStartAndPeriodEnd(LocalDate periodStart, LocalDate periodEnd);

    /** Used for upsert: find existing record for this agent + period */
    Optional<AgentReportRecord> findByAgentIdAndPeriodStartAndPeriodEnd(
            String agentId, LocalDate periodStart, LocalDate periodEnd);
}

