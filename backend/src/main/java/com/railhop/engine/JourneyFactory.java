package com.railhop.engine;

import java.util.ArrayList;
import java.util.List;

/** Turns a raw list of legs into a Journey with fare, transfer waits and risk. */
public final class JourneyFactory {
    private JourneyFactory() {}

    public static Journey build(List<Leg> legs, TravelClass cls) {
        List<Integer> waits = new ArrayList<>();
        int fare = 0;
        for (int i = 0; i < legs.size(); i++) {
            Leg leg = legs.get(i);
            fare += FareEstimator.estimate(cls, leg.km());
            if (i > 0) waits.add(leg.dep() - legs.get(i - 1).arr());
        }
        int total = legs.get(legs.size() - 1).arr() - legs.get(0).dep();
        int minWait = waits.stream().min(Integer::compare).orElse(Integer.MAX_VALUE);
        Risk risk = (waits.isEmpty() || minWait >= 150) ? Risk.LOW : (minWait >= 75 ? Risk.MEDIUM : Risk.HIGH);
        boolean overnight = waits.stream().anyMatch(w -> w >= 360);
        return new Journey(List.copyOf(legs), total, legs.size() - 1, List.copyOf(waits), fare, risk, overnight);
    }
}
