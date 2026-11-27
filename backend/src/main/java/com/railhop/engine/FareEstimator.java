package com.railhop.engine;

/**
 * Rough per-km fare model. These are ESTIMATES only - never present them as confirmed prices.
 * TODO: replace with a real fare table (class + distance slabs + reservation/superfast charges).
 */
public final class FareEstimator {
    private FareEstimator() {}

    public static int estimate(TravelClass cls, int km) {
        double perKm;
        int base;
        switch (cls) {
            case SL  -> { perKm = 0.55; base = 20; }
            case AC3 -> { perKm = 1.45; base = 40; }
            case AC2 -> { perKm = 2.10; base = 50; }
            case GEN -> { perKm = 0.35; base = 0; }
            default  -> { perKm = 0.55; base = 20; }
        }
        return (int) Math.round(base + Math.max(km, 0) * perKm);
    }
}
