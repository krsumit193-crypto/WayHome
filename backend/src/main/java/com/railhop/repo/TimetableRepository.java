package com.railhop.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

@Repository
public class TimetableRepository {
    public record RawStop(String station, Integer arrivalMin, Integer departureMin, int km) {}

    public record TrainTimetable(String number, String name, int runsOnMask, List<RawStop> stops) {}

    private final JdbcTemplate jdbc;

    public TimetableRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TrainTimetable> loadAll() {
        Map<String, TrainTimetable> trains = new LinkedHashMap<>();
        jdbc.query(
                "SELECT t.number, t.name, t.runs_on_mask, s.station_code, s.arrival_min, s.departure_min, s.distance_km "
                        + "FROM train t JOIN train_stop s ON s.train_number = t.number ORDER BY t.number, s.seq",
                (RowCallbackHandler) rs -> {
                    String no = rs.getString("number");
                    TrainTimetable t = trains.get(no);
                    if (t == null) {
                        t = new TrainTimetable(no, rs.getString("name"), rs.getInt("runs_on_mask"), new ArrayList<>());
                        trains.put(no, t);
                    }
                    t.stops().add(new RawStop(rs.getString("station_code"), nullableInt(rs, "arrival_min"),
                            nullableInt(rs, "departure_min"), rs.getInt("distance_km")));
                });
        return new ArrayList<>(trains.values());
    }

    private static Integer nullableInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }
}
