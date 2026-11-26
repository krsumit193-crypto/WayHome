package com.railhop.web;

import com.railhop.engine.Risk;
import com.railhop.engine.SortMode;
import com.railhop.engine.TravelClass;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record SearchRequest(
            @NotBlank String from,
            @NotBlank String to,
            @NotNull LocalDate date,
            TravelClass travelClass,
            @Min(0) @Max(3) Integer maxChanges,
            SortMode sort) {}

    public record LegDto(String trainNumber, String trainName,
                         String fromCode, String fromName, String toCode, String toName,
                         String departure, int departureDay, String arrival, int arrivalDay,
                         int distanceKm, int estimatedFare, String bookingUrl) {}

    public record JourneyDto(List<LegDto> legs, int durationMinutes, int changes, List<Integer> transferWaitMinutes,
                             int estimatedFare, boolean fareIsEstimate, Risk risk, boolean overnightWait) {}

    public record RouteResponse(List<JourneyDto> routes, String disclaimer) {}
}
