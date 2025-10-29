package org.mrstm.hdfareservice.strategies;

import org.mrstm.uberentityservice.models.FareRate;
import org.springframework.stereotype.Component;

@Component
public class FareStrategyImpl implements FareStrategy{
    @Override
    public double calculate(FareRate fareRate, double distance, double duration, double surge, double discount) {
        double baseFare = fareRate.getBaseFare();
        double distanceFare = fareRate.getPerKmRate();
        double minFare = fareRate.getMinFare();
        double durationFare = fareRate.getPerMinRate();

        double finalFare = (baseFare + distanceFare + durationFare) * surge;
        finalFare = Math.max(finalFare , minFare);

        if(discount > 0){
            finalFare -= finalFare * (discount / 100);
        }
        return Math.round(finalFare);
    }
}