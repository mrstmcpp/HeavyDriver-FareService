package org.mrstm.hdfareservice.repositories;

import org.mrstm.uberentityservice.dto.fare.DailyEarningsDto;
import org.mrstm.uberentityservice.models.Fare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
public interface FareRepository extends JpaRepository<Fare, Long> {
    boolean existsByBookingId(Long bookingId);

    @Query("""
                SELECT COALESCE(SUM(f.finalFare), 0)
                FROM Fare f
                WHERE f.booking.driver.id = :driverId
                  AND DATE(f.createdAt) BETWEEN :fromDate AND :toDate
            """)
    double getTotalEarningsBetween(
            @Param("driverId") Long driverId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
                SELECT new org.mrstm.uberentityservice.dto.fare.DailyEarningsDto(
                    DATE(f.createdAt),
                    SUM(f.finalFare)
                )
                FROM Fare f
                WHERE f.booking.driver.id = :driverId
                  AND DATE(f.createdAt) BETWEEN :fromDate AND :toDate
                GROUP BY DATE(f.createdAt)
                ORDER BY DATE(f.createdAt)
            """)
    List<DailyEarningsDto> getDailyEarningsBetween(
            @Param("driverId") Long driverId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );


    @Query("""
                SELECT COALESCE(SUM(f.finalFare), 0)
                FROM Fare f
                WHERE f.booking.driver.id = :driverId
                  AND MONTH(f.createdAt) = MONTH(CURRENT_DATE)
                  AND YEAR(f.createdAt) = YEAR(CURRENT_DATE)
            """)
    double getThisMonthEarnings(@Param("driverId") Long driverId);


}
