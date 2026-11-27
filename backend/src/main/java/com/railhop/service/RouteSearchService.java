package com.railhop.service;

import com.railhop.engine.Journey;
import com.railhop.engine.JourneyFactory;
import com.railhop.engine.JourneyRanker;
import com.railhop.engine.Leg;
import com.railhop.engine.RaptorEngine;
import com.railhop.engine.SortMode;
import com.railhop.engine.TravelClass;
import com.railhop.engine.TripInstance;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RouteSearchService {
    private static final int MAX_RESULTS = 12;

    private final TimetableService timetable;
    private final int minTransfer;
    private final RaptorEngine engine = new RaptorEngine();

    public RouteSearchService(TimetableService timetable,
                              @Value("${railhop.min-transfer-minutes:60}") int minTransfer) {
        this.timetable = timetable;
        this.minTransfer = minTransfer;
    }

    public List<Journey> search(String from, String to, LocalDate date, TravelClass cls, int maxChanges, SortMode sort) {
        String f = from.trim().toUpperCase();
        String t = to.trim().toUpperCase();
        if (f.equals(t)) throw new IllegalArgumentException("Origin and destination must be different.");

        List<TripInstance> trips = timetable.tripsFor(date);
        Map<String, List<Leg>> unique = new LinkedHashMap<>();

        List<List<Leg>> base = engine.search(trips, f, t, minTransfer, maxChanges);
        base.forEach(p -> unique.putIfAbsent(signature(p), p));

        // Alternatives: ban each train of a found path once and search again.
        for (List<Leg> path : base) {
            for (Leg leg : path) {
                List<TripInstance> without = trips.stream()
                        .filter(x -> !x.trainNo().equals(leg.trainNo()))
                        .toList();
                engine.search(without, f, t, minTransfer, maxChanges)
                        .forEach(p -> unique.putIfAbsent(signature(p), p));
            }
        }

        List<Journey> journeys = new ArrayList<>();
        for (List<Leg> p : unique.values()) journeys.add(JourneyFactory.build(p, cls));
        return JourneyRanker.rank(journeys, sort).stream().limit(MAX_RESULTS).toList();
    }

    private static String signature(List<Leg> legs) {
        return legs.stream().map(l -> l.trainNo() + "@" + l.dep()).collect(Collectors.joining(">"));
    }
}
