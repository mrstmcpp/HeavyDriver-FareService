package org.mrstm.hdfareservice.services;

import org.mrstm.uberentityservice.dto.fare.*;
import org.mrstm.uberentityservice.models.CarType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface FareService {
    void calculateAndSaveFare(Long bookingId);
    CalculatedFareDTO estimateFare(EstimateFareRequestDto estimateFareRequestDto, double discount);
    String addNewFareRate(FareRateDto fareRateDto);
    AnalyticsResponseDto getEarningsOfDriver(Long driverId , LocalDate fromDate , LocalDate toDate);
    List<DailyEarningsDto> getDailyEarningsBetween(Long driverId, LocalDate fromDate, LocalDate toDate);
}
