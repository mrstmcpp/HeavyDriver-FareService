package org.mrstm.hdfareservice.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.mrstm.hdfareservice.apis.GoogleMapsService;
import org.mrstm.hdfareservice.exceptions.AlreadyExistException;
import org.mrstm.hdfareservice.repositories.BookingRepository;
import org.mrstm.hdfareservice.repositories.FareRateRepository;
import org.mrstm.hdfareservice.repositories.FareRepository;
import org.mrstm.hdfareservice.strategies.FareStrategy;
import org.mrstm.uberentityservice.dto.fare.*;
import org.mrstm.uberentityservice.dto.googlemaps.DistanceDuration;
import org.mrstm.uberentityservice.models.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FareServiceImpl implements FareService {
    private final FareRateRepository fareRateRepository;
    private final FareStrategy fareStrategy;
    private final BookingRepository bookingRepository;
    private final FareRepository fareRepository;
    private final GoogleMapsService googleMapsService;

    public FareServiceImpl(FareRateRepository fareRateRepository, FareStrategy fareStrategy, BookingRepository bookingRepository, FareRepository fareRepository, GoogleMapsService googleMapsService) {
        this.fareRateRepository = fareRateRepository;
        this.fareStrategy = fareStrategy;
        this.bookingRepository = bookingRepository;
        this.fareRepository = fareRepository;
        this.googleMapsService = googleMapsService;
    }

    @Override
    @Transactional
    public void calculateAndSaveFare(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found."));
        if (fareRepository.existsByBookingId(bookingId)) {
            throw new AlreadyExistException("Fare already calculated for this booking.");
        }

        if (booking.getBookingStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot calculate fare until trip is completed.");
        }

        CarType carType = booking.getCarType();
        ExactLocation start = booking.getStartLocation();
        ExactLocation end = booking.getEndLocation();
        if (start == null || end == null) {
            throw new BadRequestException("Missing start or end location for booking.");
        }

        DistanceDuration distanceDuration = googleMapsService.getDistanceAndDuration(start, end);
        double distanceKm = distanceDuration.getDistance();
        double durationMin = Math.round(distanceDuration.getDuration());
        double surge = getSurge(distanceDuration);

        FareRate fareRate = fareRateRepository.findByCarTypeAndActiveIsTrue(carType)
                .orElseThrow(() -> new NotFoundException("Fare rate not found for car type: " + carType));

        double finalFare = fareStrategy.calculate(fareRate, distanceKm, durationMin, surge, 10);

        Fare fare = Fare.builder()
                .booking(booking)
                .carType(carType)
                .distance(distanceKm)
                .duration(durationMin)
                .finalFare(finalFare)
                .surge(surge)
                .discount(10) //discount is set to 10%
                .build();

        fareRepository.save(fare);


//        return CalculatedFareDTO.builder()
//                .fare(finalFare)
//                .distance(distanceKm)
//                .duration(durationMin)
//                .build();
    }


    @Override
    public CalculatedFareDTO estimateFare(EstimateFareRequestDto estimateFareRequestDto, double discount) {
        DistanceDuration distanceDuration = googleMapsService.getDistanceAndDuration(estimateFareRequestDto.getStartLocation(), estimateFareRequestDto.getEndLocation());
        double distance = distanceDuration.getDistance();
        double duration = Math.round(distanceDuration.getDurationInTraffic());
        double surge = getSurge(distanceDuration);
        String startAddress = distanceDuration.getStartAddress();
        String endAddress = distanceDuration.getEndAddress();


        if(estimateFareRequestDto.getCarType().isEmpty()){
            throw new IllegalArgumentException("Invalid car type provided.");
        }
        FareRate fareRate = fareRateRepository.findByCarTypeAndActiveIsTrue(CarType.valueOf(estimateFareRequestDto.getCarType()))
                .orElseThrow(() -> new NotFoundException("Fare rate not found for this car type."));
        double finalFare = fareStrategy.calculate(fareRate, distance, duration, surge, discount);


        return CalculatedFareDTO.builder()
                .startAddress(startAddress)
                .endAddress(endAddress)
                .duration(duration)
                .distance(distance)
                .fare(finalFare)
                .build();
    }

    @Override
    public String addNewFareRate(FareRateDto fareRateDto) {
        FareRate fareRate = FareRate.builder()
                .minFare(fareRateDto.getMinFare())
                .perKmRate(fareRateDto.getPerKmRate())
                .perMinRate(fareRateDto.getPerMinRate())
                .baseFare(fareRateDto.getBaseFare())
                .carType(CarType.valueOf(fareRateDto.getCarType().toUpperCase()))
                .active(true)
                .build();
        fareRateRepository.save(fareRate);
        return "SUCCESS";
    }

    @Override
    public AnalyticsResponseDto getEarningsOfDriver(Long driverId, LocalDate fromDate, LocalDate toDate) {
        double totalEarnings = fareRepository.getTotalEarningsBetween(driverId, fromDate, toDate);
        double thisMonthEarnings = fareRepository.getThisMonthEarnings(driverId);
        double pendingEarnings = 0;
        double withdrawnEarnings = totalEarnings; //baad me

        return AnalyticsResponseDto.builder()
                .totalEarnings(totalEarnings)
                .thisMonthEarnings(thisMonthEarnings)
                .pendingEarnings(pendingEarnings)
                .withdrawnEarnings(withdrawnEarnings)
                .build();
    }

    @Override
    public List<DailyEarningsDto> getDailyEarningsBetween(Long driverId, LocalDate fromDate, LocalDate toDate) {
        return fareRepository.getDailyEarningsBetween(driverId, fromDate, toDate);
    }


    private double getSurge(DistanceDuration distanceDuration) {
        double durationMin = distanceDuration.getDuration();
        double durationInTraffic = distanceDuration.getDurationInTraffic();

        if (durationMin <= 0) return 1.0;

        double trafficRatio = durationInTraffic / durationMin;
        double surge;

        if (trafficRatio <= 1.0) {
            surge = 1.0;
        } else if (trafficRatio <= 1.2) {
            surge = 1.0 + (trafficRatio - 1.0) * 1.0;
        } else if (trafficRatio <= 1.5) {
            surge = 1.2 + (trafficRatio - 1.2) * 1.0;
        } else if (trafficRatio <= 2.0) {
            surge = 1.5 + (trafficRatio - 1.5) * 1.0;
        } else {
            surge = 2.5;
        }

        surge = Math.round(surge * 100.0) / 100.0;
        return surge;
    }

}
