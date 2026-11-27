package com.railhop.engine;

/** One stop of a concrete trip. Times are minutes since midnight of the JOURNEY date (can be negative or > 1440). */
public record StopTime(String station, int arr, int dep, int km) {}
