package com.sbab.assignment.service;

import com.sbab.assignment.adapter.sl.SlBusStop;
import com.sbab.assignment.adapter.sl.SlClient;
import com.sbab.assignment.adapter.sl.SlLine;
import com.sbab.assignment.adapter.sl.Tuple;
import com.sbab.assignment.domain.BusLine;
import com.sbab.assignment.domain.BusStop;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.TimeoutStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SlJourCacheService extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SlJourCacheService.class);
    private TimeoutStream periodicJob;
    private final SlClient slClient;

    private List<BusLine> topLongestBusLines = List.of();

    public SlJourCacheService(SlClient slClient) {
        this.slClient = slClient;
    }

    @Override
    public void start() {
        periodicJob = vertx.periodicStream(0, 36000)
                .handler(__ -> this.updateCache())
                .exceptionHandler(cause -> {
                });
    }

    private void updateCache() {

        slClient.getAllSlBusesWithStopIds()
                .flatMap(slLines -> slClient.getAllSlBusStops().map(slStops ->
                        new Tuple(slLines, slStops)))
                .onSuccess(result -> {
                    List<BusLineWithStopNumber> busLinesWithStops = getBusLineWithStopNumbers(result);
                    Function<Integer, String> getStopNameById = stopId -> result.slStops()
                            .stream()
                            .filter(stop -> stopId.equals(Integer.parseInt(stop.stopPointNumber())))
                            .findFirst()
                            .map(SlBusStop::stopPointName)
                            .orElse("Missing stopName");

                    topLongestBusLines = busLinesWithStops.stream()
                            .map(line -> transform(line, getStopNameById))
                            .collect(Collectors.toList());
                    logger.info("Updating SL Bus info cache with {}", topLongestBusLines);
                })
                .onFailure(cause -> logger.error("Exception occurred while updating cache, cause: ", cause));
    }

    private List<BusLineWithStopNumber> getBusLineWithStopNumbers(Tuple result) {
        Map<String, List<SlLine>> slLineByLineNumber = result.slLines()
                .stream()
                .collect(Collectors.groupingBy(SlLine::lineNumber));
        return slLineByLineNumber.entrySet().stream()
                .map(this::transform)
                .sorted(Comparator.comparing(line -> line.busStops().size(), Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private BusLine transform(BusLineWithStopNumber line, Function<Integer, String> getStopNameById) {
        var busStops = line.busStops.stream().map(stopNumber -> new BusStop(stopNumber, getStopNameById.apply(stopNumber))).collect(Collectors.toSet());
        return new BusLine(line.number, busStops);
    }

    private BusLineWithStopNumber transform(Map.Entry<String, List<SlLine>> slLine) {
        int busLineNumber = Integer.parseInt(slLine.getKey());
        Set<Integer> busStopNumbers = slLine.getValue().stream().filter(busStop -> "1".equals(busStop.directionCode()))
                .map(SlLine::journeyPatternPointNumber).map(Integer::parseInt).collect(Collectors.toSet());

        return new BusLineWithStopNumber(busLineNumber, busStopNumbers);
    }

    @Override
    public void stop() {
        periodicJob.cancel();
    }

    public List<BusLine> getTopLongestBusLines() {
        return Collections.unmodifiableList(topLongestBusLines);
    }

    record BusLineWithStopNumber(int number, Set<Integer> busStops) {

    }
}


