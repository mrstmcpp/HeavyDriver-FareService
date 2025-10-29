package org.mrstm.hdfareservice.strategies;

import org.mrstm.uberentityservice.models.FareRate;

public interface FareStrategy {
    double calculate(FareRate fareRate , double distance , double duration , double surge, double discount);
}
