package com.sbab.assignment.service

import com.sbab.assignment.adapter.sl.SlBusStop
import com.sbab.assignment.adapter.sl.SlClient
import com.sbab.assignment.adapter.sl.SlLine
import com.sbab.assignment.domain.BusLine
import com.sbab.assignment.domain.BusStop
import io.vertx.core.Future
import io.vertx.core.Vertx
import org.awaitility.Awaitility
import spock.lang.Specification

import java.time.Duration

class SlJourCacheServiceSpec extends Specification {

    SlJourCacheService slJourCacheService
    SlClient slClient
    Vertx vertx


    def setup() {
        vertx = Vertx.vertx()
        slClient = Mock(SlClient) {
            getAllSlBusesWithStopIds() >> Future.succeededFuture([new SlLine("1", "1", "1001"),
                                                                  new SlLine("1", "1", "1002"),
                                                                  new SlLine("1", "1", "1003"),
                                                                  new SlLine("2", "1", "1004"),
                                                                  new SlLine("2", "1", "1005"),
                                                                  new SlLine("2", "1", "1006"),
                                                                  new SlLine("2", "1", "1007"),
                                                                  new SlLine("2", "1", "1008"),
                                                                  new SlLine("2", "1", "1009"),
                                                                  new SlLine("3", "1", "1010"),
                                                                  new SlLine("3", "1", "1011"),
                                                                  new SlLine("4", "1", "1012"),
                                                                  new SlLine("4", "1", "1001"),
                                                                  new SlLine("4", "1", "1003"),
                                                                  new SlLine("4", "1", "1004")
            ])

            getAllSlBusStops() >> Future.succeededFuture([new SlBusStop("1001", "Stop A"),
                                                          new SlBusStop("1002", "Stop B"),
                                                          new SlBusStop("1003", "Stop C"),
                                                          new SlBusStop("1004", "Stop D"),
                                                          new SlBusStop("1005", "Stop E"),
                                                          new SlBusStop("1006", "Stop F"),
                                                          new SlBusStop("1007", "Stop G"),
                                                          new SlBusStop("1008", "Stop H"),
                                                          new SlBusStop("1009", "Stop I"),
                                                          new SlBusStop("1010", "Stop J"),
                                                          new SlBusStop("1011", "Stop K"),
                                                          new SlBusStop("1012", "Stop L")])
        }

    }

    def "should eventually update busLine cache"() {
        when:
        slJourCacheService = new SlJourCacheService(slClient)
        vertx.deployVerticle(slJourCacheService)

        then:
        Awaitility.await().atMost(Duration.ofSeconds(1)).until {
            slJourCacheService.topLongestBusLines == [new BusLine(2, [new BusStop(1004, "Stop D"),
                                                                      new BusStop(1005, "Stop E"),
                                                                      new BusStop(1006, "Stop F"),
                                                                      new BusStop(1007, "Stop G"),
                                                                      new BusStop(1008, "Stop H"),
                                                                      new BusStop(1009, "Stop I")] as Set),
                                                      new BusLine(4, [new BusStop(1012, "Stop L"),
                                                                      new BusStop(1001, "Stop A"),
                                                                      new BusStop(1003, "Stop C"),
                                                                      new BusStop(1004, "Stop D")] as Set),
                                                      new BusLine(1, [new BusStop(1001, "Stop A"),
                                                                      new BusStop(1002, "Stop B"),
                                                                      new BusStop(1003, "Stop C")] as Set),
                                                      new BusLine(3, [new BusStop(1010, "Stop J"),
                                                                      new BusStop(1011, "Stop K")] as Set)]
        }
    }
}
