package com.sbab.assignment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sbab.assignment.ObjectMapperProvider;
import com.sbab.assignment.api.ApiBusLineWithStops;
import com.sbab.assignment.api.ApiResponse;
import com.sbab.assignment.domain.BusLine;
import com.sbab.assignment.domain.BusStop;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ApiService extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    private final int port;
    private final SlJourCacheService cacheService;

    private HttpServer httpServer;

    public ApiService(SlJourCacheService cacheService, int port) {
        this.port = port;
        this.cacheService = cacheService;
    }


    @Override
    public void start() {

        Router api = Router.router(vertx);
        api.get("/api/top10BusLines")
                .handler(this::HandleRequest);
        vertx.createHttpServer()
                .requestHandler(api)
                .listen(port)
                .onSuccess(server -> {
                    logger.info("Started webserver at port {}", port);
                    httpServer = server;
                });
    }

    @Override
    public void stop() {
        httpServer.close()
                .onSuccess(__ -> logger.info("webserver closed now!"));
    }

    private void HandleRequest(RoutingContext routingContext) {
        try {
            List<BusLine> busLines = cacheService.getTopLongestBusLines();
            if (!busLines.isEmpty()) {
                var busLineWithMostStops = new ApiBusLineWithStops(busLines.get(0).number(),
                        busLines.get(0).busStops().stream().map(BusStop::name).collect(Collectors.toSet()));
                var otherBusLines = busLines.stream().map(BusLine::number)
                        .filter(number -> number != busLineWithMostStops.line())
                        .collect(Collectors.toSet());
                var response = new ApiResponse(busLineWithMostStops, otherBusLines);
                routingContext.response().setStatusCode(200)
                        .end(ObjectMapperProvider.OBJECT_MAPPER.writeValueAsString(response));
            } else {
                routingContext.response().setStatusCode(503)
                        .end("Serivce Unavailable");
            }
        } catch (JsonProcessingException e) {
            logger.error("Exception while generating response", e);
            routingContext.response().setStatusCode(500)
                    .end("Internal Error, check logs for details");
        }
    }
}
