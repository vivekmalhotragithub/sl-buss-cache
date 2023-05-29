package com.sbab.assignment.domain;

import java.util.Set;

public record BusLine(int number, Set<BusStop> busStops) {
}
