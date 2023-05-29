package com.sbab.assignment.adapter.sl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

public class SlClient implements AutoCloseable {

    private static final String URL_SL_BUSES_WITH_STOPS = "/LineData.json?model=jour&DefaultTransportModeCode=BUS&key=";
    private static final String URL_SL_ALL_STOPS = "/LineData.json?model=stop&DefaultTransportModeCode=BUS&key=";
    private static final Logger logger = LoggerFactory.getLogger(SlClient.class);
    private final WebClient webClient;
    private final String slBaseUrl;
    private final String appKey;

    public SlClient(Vertx vertx, String slBaseUrl, String appKey) {
        this.webClient = WebClient.create(vertx);
        this.slBaseUrl = slBaseUrl;
        this.appKey = appKey;
    }

    public Future<List<SlLine>> getAllSlBusesWithStopIds() {
        return get(slBaseUrl + URL_SL_BUSES_WITH_STOPS + appKey, buffer -> Json.decodeValue(buffer, SlJour.class))
                .map(slJour -> slJour.responseData().result());
    }

    public Future<List<SlBusStop>> getAllSlBusStops() {
        return get(slBaseUrl + URL_SL_ALL_STOPS + appKey, buffer -> Json.decodeValue(buffer, SlStops.class))
                .map(slStops -> slStops.responseData().result());
    }

    public <T> Future<T> get(String url, Function<Buffer, T> mapper) {
        return webClient.getAbs(url)
                .send()
                .transform(result -> {
                    var response = result.result();
                    if (result.succeeded()) {
                        if (response.statusCode() >= 200 && response.statusCode() <= 299) {
                            logger.info("Success({}) calling {}", response.statusCode(), url);
                            return Future.succeededFuture(mapper.apply(response.body()));
                        } else {
                            logger.warn("Failure({}) calling {}", response.statusCode(), url);
                            return Future.failedFuture(new HttpException(response.statusCode(), response.bodyAsString()));
                        }

                    } else {
                        var cause = result.cause();
                        logger.warn("Exception when calling {} cause: {}", response.statusCode(), cause.getMessage(), cause);
                        return Future.failedFuture(cause);
                    }
                });
    }

    @Override
    public void close() {
        webClient.close();
    }
}
