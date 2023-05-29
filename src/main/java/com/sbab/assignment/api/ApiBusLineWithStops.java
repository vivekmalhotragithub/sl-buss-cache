package com.sbab.assignment.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record ApiBusLineWithStops(@JsonProperty int line ,@JsonProperty Set<String> stopNames) {
}
