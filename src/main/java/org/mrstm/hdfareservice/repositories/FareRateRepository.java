package org.mrstm.hdfareservice.repositories;

import org.mrstm.uberentityservice.models.CarType;
import org.mrstm.uberentityservice.models.FareRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareRateRepository extends JpaRepository<FareRate , Long> {
    Optional<FareRate> findByCarTypeAndActiveIsTrue(CarType carType);

}
