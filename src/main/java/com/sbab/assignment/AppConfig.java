package com.sbab.assignment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppConfig(@JsonProperty String slBaseUrl,
                        @JsonProperty long refreshCacheInSeconds,
                        @JsonProperty int longestBusLinesTopLimit,
                        @JsonProperty String appKey,
                        @JsonProperty int port) {
}
