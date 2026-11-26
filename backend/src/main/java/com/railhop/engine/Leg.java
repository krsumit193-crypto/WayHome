package com.railhop.engine;

public record Leg(String trainNo, String trainName, String from, String to, int dep, int arr, int km) {}
