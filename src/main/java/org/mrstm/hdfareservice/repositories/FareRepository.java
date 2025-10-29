package org.mrstm.hdfareservice.repositories;

import org.mrstm.uberentityservice.models.Fare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FareRepository extends JpaRepository<Fare , Long> {
    boolean existsByBookingId(Long bookingId);

}
