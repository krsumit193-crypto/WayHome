package com.railhop.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RaptorEngineTest {
    private final RaptorEngine engine = new RaptorEngine();

    private static TripInstance trip(String no, StopTime... stops) {
        return new TripInstance(no, "Train " + no, List.of(stops));
    }

    // T1: A 08:00 -> B 10:00      T2: B 11:00 -> C 13:00
    private static List<TripInstance> twoTrainChain(int t2Departure) {
        return List.of(
                trip("T1", new StopTime("A", 480, 480, 0), new StopTime("B", 600, 600, 100)),
                trip("T2", new StopTime("B", t2Departure, t2Departure, 0),
                        new StopTime("C", t2Departure + 120, t2Departure + 120, 150)));
    }

    @Test
    void findsOneChangeRoute() {
        List<List<Leg>> res = engine.search(twoTrainChain(660), "A", "C", 60, 1);
        assertEquals(1, res.size());
        assertEquals(2, res.get(0).size());
        assertEquals("T2", res.get(0).get(1).trainNo());
    }

    @Test
    void rejectsTransferShorterThanMinimum() {
        List<List<Leg>> res = engine.search(twoTrainChain(630), "A", "C", 60, 1);
        assertTrue(res.isEmpty());
    }

    @Test
    void respectsMaxChanges() {
        List<List<Leg>> res = engine.search(twoTrainChain(660), "A", "C", 60, 0);
        assertTrue(res.isEmpty());
    }

    @Test
    void returnsDirectAndFasterConnectingOption() {
        List<TripInstance> trips = new java.util.ArrayList<>(twoTrainChain(660));
        trips.add(trip("D1", new StopTime("A", 500, 500, 0), new StopTime("C", 900, 900, 250)));
        List<List<Leg>> res = engine.search(trips, "A", "C", 60, 1);
        assertEquals(2, res.size());
        assertEquals(1, res.get(0).size());
        assertEquals(2, res.get(1).size());
    }

    @Test
    void handlesTrainsPastMidnight() {
        List<TripInstance> trips = List.of(
                trip("N1", new StopTime("A", 1200, 1200, 0), new StopTime("B", 1500, 1500, 400)),
                trip("N2", new StopTime("B", 1620, 1620, 0), new StopTime("C", 1700, 1700, 90)));
        List<List<Leg>> res = engine.search(trips, "A", "C", 60, 1);
        assertEquals(1, res.size());
    }
}
