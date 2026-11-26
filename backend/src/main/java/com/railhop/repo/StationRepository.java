package com.railhop.repo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StationRepository {
    public record StationDto(String code, String name) {}

    private final JdbcTemplate jdbc;
    private volatile Map<String, String> nameCache;

    public StationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<StationDto> search(String q) {
        String needle = q.trim().toLowerCase();
        String like = "%" + needle + "%";
        return jdbc.query(
                "SELECT code, name FROM station WHERE LOWER(name) LIKE ? OR LOWER(code) LIKE ? "
                        + "ORDER BY CASE WHEN LOWER(code) = ? THEN 0 ELSE 1 END, name LIMIT 10",
                (rs, i) -> new StationDto(rs.getString("code"), rs.getString("name")),
                like, like, needle);
    }

    /** code -> display name. Cached after first load (stations rarely change). */
    public Map<String, String> names() {
        Map<String, String> c = nameCache;
        if (c == null) {
            Map<String, String> m = new HashMap<>();
            jdbc.query("SELECT code, name FROM station", rs -> {
                m.put(rs.getString("code"), rs.getString("name"));
            });
            nameCache = c = m;
        }
        return c;
    }
}
