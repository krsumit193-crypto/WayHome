package com.railhop.engine;

import java.util.Comparator;
import java.util.List;

public final class JourneyRanker {
    private JourneyRanker() {}

    public static List<Journey> rank(List<Journey> journeys, SortMode mode) {
        Comparator<Journey> byTime = Comparator.comparingInt(Journey::totalMinutes);
        Comparator<Journey> cmp = switch (mode) {
            case CHEAPEST -> Comparator.comparingInt(Journey::fare).thenComparing(byTime);
            case FEWEST_CHANGES -> Comparator.comparingInt(Journey::changes).thenComparing(byTime);
            case SAFEST -> Comparator.<Journey>comparingInt(j -> j.risk().ordinal()).thenComparing(byTime);
            default -> byTime.thenComparingInt(Journey::fare);
        };
        return journeys.stream().sorted(cmp).toList();
    }
}
