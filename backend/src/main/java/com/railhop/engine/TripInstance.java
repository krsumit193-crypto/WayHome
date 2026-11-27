package com.railhop.engine;

import java.util.List;

/** A train running on one specific calendar day, with absolute stop times. */
public record TripInstance(String trainNo, String trainName, List<StopTime> stops) {}
