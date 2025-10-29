package org.mrstm.hdfareservice.apis;

import org.mrstm.uberentityservice.dto.googlemaps.DistanceDuration;
import org.mrstm.uberentityservice.models.ExactLocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Service
public class GoogleMapsServiceImpl implements GoogleMapsService{
    private final RestTemplate restTemplate;
    @Value("${google.maps.api-key}")
    private String apiKey;

    public GoogleMapsServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public DistanceDuration getDistanceAndDuration(ExactLocation startLocation, ExactLocation endLocation) {
        double startLat = startLocation.getLatitude();
        double startLng = startLocation.getLongitude();
        double endLat = endLocation.getLatitude();
        double endLng = endLocation.getLongitude();
        String url = String.format("https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s,%s&destinations=%s,%s&departure_time=now&key=%s",
                startLat,startLng , endLat, endLng , apiKey
                );
        var res = restTemplate.getForObject(url , Map.class);
        var element = ((List<Map>) ((Map) ((List<?>) res.get("rows")).get(0)).get("elements")).get(0);

        double distance = ((Number) ((Map) element.get("distance")).get("value")).doubleValue() / 1000.0;
        double duration = ((Number) ((Map) element.get("duration")).get("value")).doubleValue() / 60.0;
        double durationInTraffic = ((Number) ((Map) element.get("duration_in_traffic")).get("value")).doubleValue() / 60.0;

        return DistanceDuration.builder()
                .distance(distance)
                .duration(duration)
                .durationInTraffic(durationInTraffic)
                .build();

    }
}
