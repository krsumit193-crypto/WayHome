package com.railhop.web;

import com.railhop.engine.FareEstimator;
import com.railhop.engine.Journey;
import com.railhop.engine.Leg;
import com.railhop.engine.SortMode;
import com.railhop.engine.TravelClass;
import com.railhop.repo.StationRepository;
import com.railhop.repo.StationRepository.StationDto;
import com.railhop.service.RouteSearchService;
import com.railhop.web.Dtos.JourneyDto;
import com.railhop.web.Dtos.LegDto;
import com.railhop.web.Dtos.RouteResponse;
import com.railhop.web.Dtos.SearchRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RouteController {
    // Generic official entry point. TODO: deep-link per leg when an authorised integration exists.
    private static final String IRCTC_URL = "https://www.irctc.co.in/nget/train-search";
    private static final String DISCLAIMER =
            "Fares are estimates and seat availability is not checked. Confirm on the official IRCTC site before travelling.";

    private final RouteSearchService search;
    private final StationRepository stations;

    public RouteController(RouteSearchService search, StationRepository stations) {
        this.search = search;
        this.stations = stations;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/stations")
    public List<StationDto> stations(@RequestParam String q) {
        return q.trim().length() < 2 ? List.of() : stations.search(q);
    }

    @PostMapping("/routes/search")
    public RouteResponse routes(@Valid @RequestBody SearchRequest req) {
        TravelClass cls = req.travelClass() == null ? TravelClass.ANY : req.travelClass();
        int maxChanges = req.maxChanges() == null ? 2 : req.maxChanges();
        SortMode sort = req.sort() == null ? SortMode.FASTEST : req.sort();

        List<Journey> journeys = search.search(req.from(), req.to(), req.date(), cls, maxChanges, sort);
        Map<String, String> names = stations.names();
        List<JourneyDto> dtos = journeys.stream().map(j -> toDto(j, cls, names)).toList();
        return new RouteResponse(dtos, DISCLAIMER);
    }

    private JourneyDto toDto(Journey j, TravelClass cls, Map<String, String> names) {
        List<LegDto> legs = j.legs().stream().map(l -> toLeg(l, cls, names)).toList();
        return new JourneyDto(legs, j.totalMinutes(), j.changes(), j.transferWaits(), j.fare(), true,
                j.risk(), j.overnightWait());
    }

    private LegDto toLeg(Leg l, TravelClass cls, Map<String, String> names) {
        return new LegDto(l.trainNo(), l.trainName(),
                l.from(), names.getOrDefault(l.from(), l.from()),
                l.to(), names.getOrDefault(l.to(), l.to()),
                hhmm(l.dep()), Math.floorDiv(l.dep(), 1440), hhmm(l.arr()), Math.floorDiv(l.arr(), 1440),
                l.km(), FareEstimator.estimate(cls, l.km()), IRCTC_URL);
    }

    private static String hhmm(int minutes) {
        int t = Math.floorMod(minutes, 1440);
        return String.format("%02d:%02d", t / 60, t % 60);
    }
}
