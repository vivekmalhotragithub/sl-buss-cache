package com.sbab.assignment.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ApiResponse(@JsonProperty ApiBusLineWithStops busLineWithMostStops,
                          @JsonProperty Set<Integer> otherBusLinesWithMostStops) {
}
