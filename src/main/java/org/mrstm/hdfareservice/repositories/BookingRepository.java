package org.mrstm.hdfareservice.repositories;

import org.mrstm.uberentityservice.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking , Long> {
}
