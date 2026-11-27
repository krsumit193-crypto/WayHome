package com.railhop.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplified RAPTOR (round-based public transit routing).
 * Round k = best arrival at every station using exactly k trains, so "max changes" == rounds - 1.
 * A transfer needs: next departure >= previous arrival + minTransfer.
 *
 * Returns one path per round in which the destination got a strictly earlier arrival
 * (a Pareto set: fewer changes vs. earlier arrival).
 *
 * TODO (performance): index trips by station instead of scanning all trips every round.
 */
public class RaptorEngine {
    private static final int INF = Integer.MAX_VALUE;

    private record Label(int arr, TripInstance trip, int boardIdx, int alightIdx, String prevStation) {}

    public List<List<Leg>> search(List<TripInstance> trips, String from, String to, int minTransfer, int maxChanges) {
        Map<String, Integer> best = new HashMap<>();
        List<Map<String, Label>> rounds = new ArrayList<>();

        Map<String, Label> start = new HashMap<>();
        start.put(from, new Label(0, null, -1, -1, null));
        rounds.add(start);
        best.put(from, 0);

        List<List<Leg>> results = new ArrayList<>();
        for (int k = 1; k <= maxChanges + 1; k++) {
            Map<String, Label> prev = rounds.get(k - 1);
            if (prev.isEmpty()) break;
            int buffer = (k == 1) ? 0 : minTransfer;
            Map<String, Label> cur = new HashMap<>();

            for (TripInstance trip : trips) {
                List<StopTime> stops = trip.stops();
                int boardIdx = -1;
                for (int i = 0; i < stops.size(); i++) {
                    StopTime s = stops.get(i);
                    if (boardIdx >= 0) {
                        Label already = cur.get(s.station());
                        int known = Math.min(best.getOrDefault(s.station(), INF), already == null ? INF : already.arr());
                        if (s.arr() < known) {
                            cur.put(s.station(), new Label(s.arr(), trip, boardIdx, i, stops.get(boardIdx).station()));
                        }
                    }
                    if (boardIdx < 0 && i < stops.size() - 1) {
                        Label p = prev.get(s.station());
                        if (p != null && p.arr() + buffer <= s.dep()) boardIdx = i;
                    }
                }
            }

            for (Map.Entry<String, Label> e : cur.entrySet()) best.put(e.getKey(), e.getValue().arr());
            rounds.add(cur);
            if (cur.containsKey(to)) results.add(reconstruct(rounds, k, to));
        }
        return results;
    }

    private List<Leg> reconstruct(List<Map<String, Label>> rounds, int k, String to) {
        Deque<Leg> legs = new ArrayDeque<>();
        String station = to;
        for (int r = k; r >= 1; r--) {
            Label l = rounds.get(r).get(station);
            StopTime board = l.trip().stops().get(l.boardIdx());
            StopTime alight = l.trip().stops().get(l.alightIdx());
            legs.addFirst(new Leg(l.trip().trainNo(), l.trip().trainName(), board.station(), alight.station(),
                    board.dep(), alight.arr(), alight.km() - board.km()));
            station = l.prevStation();
        }
        return new ArrayList<>(legs);
    }
}
