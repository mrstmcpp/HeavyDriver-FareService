package org.mrstm.hdfareservice.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.mrstm.hdfareservice.apis.GoogleMapsService;
import org.mrstm.hdfareservice.repositories.BookingRepository;
import org.mrstm.hdfareservice.repositories.FareRateRepository;
import org.mrstm.hdfareservice.repositories.FareRepository;
import org.mrstm.hdfareservice.strategies.FareStrategy;
import org.mrstm.uberentityservice.dto.fare.CalculatedFareDTO;
import org.mrstm.uberentityservice.dto.fare.EstimateFareRequestDto;
import org.mrstm.uberentityservice.dto.googlemaps.DistanceDuration;
import org.mrstm.uberentityservice.models.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FareServiceImpl implements FareService{
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
    public CalculatedFareDTO calculateAndSaveFare(Long bookingId, CarType carType, double discount) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found."));
        if (fareRepository.existsByBookingId(bookingId)) {
            return CalculatedFareDTO.builder()
                    .distance(booking.getFareHistory().getDistance())
                    .duration(booking.getFareHistory().getDuration())
                    .fare(booking.getFareHistory().getFinalFare())
                    .build();
        }


        if (booking.getBookingStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot calculate fare until trip is completed.");
        }

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

        double finalFare = fareStrategy.calculate(fareRate, distanceKm, durationMin, surge, discount);

        Fare fare = Fare.builder()
                .booking(booking)
                .carType(carType)
                .distance(distanceKm)
                .duration(durationMin)
                .finalFare(finalFare)
                .surge(surge)
                .discount(discount)
                .build();

        fareRepository.save(fare);


        return CalculatedFareDTO.builder()
                .fare(finalFare)
                .distance(distanceKm)
                .duration(durationMin)
                .build();
    }


    @Override
    public CalculatedFareDTO estimateFare(EstimateFareRequestDto estimateFareRequestDto, double discount) {
        DistanceDuration distanceDuration = googleMapsService.getDistanceAndDuration(estimateFareRequestDto.getStartLocation(), estimateFareRequestDto.getEndLocation());
        double distance = distanceDuration.getDistance();
        double duration = Math.round(distanceDuration.getDuration());
        double surge = getSurge(distanceDuration);

        FareRate fareRate = fareRateRepository.findByCarTypeAndActiveIsTrue(CarType.valueOf(estimateFareRequestDto.getCarType()))
                .orElseThrow(() -> new NotFoundException("Fare rate not found for this car type."));
        double finalFare = fareStrategy.calculate(fareRate , distance, duration , surge , discount);
        return CalculatedFareDTO.builder()
                .duration(duration)
                .distance(distance)
                .fare(finalFare)
                .build();
    }

    @Override
    public String addNewFareRate(FareRate fareRate) {
        fareRateRepository.save(fareRate);
        return "SUCCESS";
    }


    private double getSurge(DistanceDuration distanceDuration){
        double durationMin = Math.round(distanceDuration.getDuration());
        double durationInTraffic = distanceDuration.getDurationInTraffic();

        double trafficRatio = durationInTraffic / durationMin;
        double surge;

        if (trafficRatio <= 1.1) {
            surge = 2.0;
        } else if (trafficRatio <= 1.4) {
            surge = 2.0 + (trafficRatio - 1.1) * (0.5 / 0.3);
        } else if (trafficRatio <= 2.0) {
            surge = 2.5 + (trafficRatio - 1.4) * (1.5 / 0.6);
        } else {
            surge = 4.0;
        }

        surge = Math.round(surge * 100.0) / 100.0;
        return surge;
    }
}
