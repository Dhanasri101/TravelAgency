package com.epam.edp.demo.repository;

import com.epam.edp.demo.enums.BookingState;
import com.epam.edp.demo.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByUserId(String userId);

    List<Booking> findByTourId(String tourId);

    List<Booking> findByState(BookingState state);
}

