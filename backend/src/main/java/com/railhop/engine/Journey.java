package com.railhop.engine;

import java.util.List;

public record Journey(List<Leg> legs, int totalMinutes, int changes, List<Integer> transferWaits,
                      int fare, Risk risk, boolean overnightWait) {}
