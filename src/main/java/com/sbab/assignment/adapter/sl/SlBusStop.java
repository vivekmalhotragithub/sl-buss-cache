package com.sbab.assignment.adapter.sl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record SlBusStop(@JsonProperty("StopPointNumber") String stopPointNumber,
                        @JsonProperty("StopPointName") String stopPointName) {
}
