package com.sbab.assignment.adapter.sl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record SlLine(@JsonProperty("LineNumber") String lineNumber,
                     @JsonProperty("DirectionCode") String directionCode,
                     @JsonProperty("JourneyPatternPointNumber") String journeyPatternPointNumber) {
}
