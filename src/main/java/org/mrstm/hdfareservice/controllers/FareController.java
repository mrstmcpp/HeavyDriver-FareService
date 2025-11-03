package org.mrstm.hdfareservice.controllers;

import org.mrstm.hdfareservice.services.FareService;
import org.mrstm.uberentityservice.dto.fare.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/fare")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    public ResponseEntity<CalculatedFareDTO> estimateFare(@RequestBody EstimateFareRequestDto estimateFareRequestDto) {
        return new ResponseEntity<>(fareService.estimateFare(estimateFareRequestDto, 10), HttpStatus.OK);
    }

    @PostMapping("/add-rate") // just for production
    public ResponseEntity<String> addNewFareRate(@RequestBody FareRateDto fareRateDto) {
        return new ResponseEntity<>(fareService.addNewFareRate(fareRateDto), HttpStatus.CREATED);
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponseDto> getEarningsOfDriver(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        // Optional: only allow drivers to access this endpoint
        if (!"DRIVER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (fromDate == null || toDate == null) {
            toDate = LocalDate.now();
            fromDate = toDate.minusDays(30);
        }

        return ResponseEntity.ok(fareService.getEarningsOfDriver(userId, fromDate, toDate));
    }

    @GetMapping("/analytics/daily")
    public ResponseEntity<List<DailyEarningsDto>> getDailyEarningsOfDriver(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        if (!"DRIVER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (fromDate == null || toDate == null) {
            toDate = LocalDate.now();
            fromDate = toDate.minusDays(30);
        }

        return ResponseEntity.ok(fareService.getDailyEarningsBetween(userId, fromDate, toDate));
    }
}
