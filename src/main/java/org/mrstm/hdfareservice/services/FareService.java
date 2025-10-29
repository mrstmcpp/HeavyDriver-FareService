package org.mrstm.hdfareservice.services;

import org.mrstm.uberentityservice.dto.fare.CalculatedFareDTO;
import org.mrstm.uberentityservice.dto.fare.EstimateFareRequestDto;
import org.mrstm.uberentityservice.models.CarType;
import org.mrstm.uberentityservice.models.FareRate;
import org.springframework.stereotype.Service;

@Service
public interface FareService {
    CalculatedFareDTO calculateAndSaveFare(Long bookingId , CarType carType , double discount);
    CalculatedFareDTO estimateFare(EstimateFareRequestDto estimateFareRequestDto, double discount);
    String addNewFareRate(FareRate fareRate);
}
