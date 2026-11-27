package com.railhop.service;

import com.railhop.engine.StopTime;
import com.railhop.engine.TripInstance;
import com.railhop.repo.TimetableRepository;
import com.railhop.repo.TimetableRepository.RawStop;
import com.railhop.repo.TimetableRepository.TrainTimetable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Expands the weekly timetable into concrete trips around a journey date.
 * Start offsets -1..+3 cover trains that left yesterday and multi-day journeys.
 */
@Service
public class TimetableService {
    private static final int MINUTES_PER_DAY = 1440;

    private final TimetableRepository repo;
    private volatile List<TrainTimetable> cache;

    public TimetableService(TimetableRepository repo) {
        this.repo = repo;
    }

    private List<TrainTimetable> timetable() {
        List<TrainTimetable> c = cache;
        if (c == null) {
            synchronized (this) {
                if (cache == null) cache = repo.loadAll();
                c = cache;
            }
        }
        return c;
    }

    public List<TripInstance> tripsFor(LocalDate journeyDate) {
        List<TripInstance> out = new ArrayList<>();
        for (TrainTimetable t : timetable()) {
            for (int offset = -1; offset <= 3; offset++) {
                int weekdayBit = journeyDate.plusDays(offset).getDayOfWeek().getValue() - 1;
                if (((t.runsOnMask() >> weekdayBit) & 1) == 0) continue;
                List<StopTime> stops = new ArrayList<>();
                for (RawStop s : t.stops()) {
                    int arr = s.arrivalMin() != null ? s.arrivalMin() : s.departureMin();
                    int dep = s.departureMin() != null ? s.departureMin() : s.arrivalMin();
                    stops.add(new StopTime(s.station(), offset * MINUTES_PER_DAY + arr,
                            offset * MINUTES_PER_DAY + dep, s.km()));
                }
                out.add(new TripInstance(t.number(), t.name(), stops));
            }
        }
        return out;
    }
}
