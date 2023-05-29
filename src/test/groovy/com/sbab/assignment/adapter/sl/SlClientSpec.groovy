package com.sbab.assignment.adapter.sl

import com.sbab.assignment.ObjectMapperProvider
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.handler.HttpException
import org.awaitility.Awaitility
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.Parameter
import spock.lang.Specification

import java.time.Duration

class SlClientSpec extends Specification {
    Integer port
    SlClient slClient
    ClientAndServer mockServer

    def setup() {
        port = 2000 + new Random().nextInt(200)
        mockServer = ClientAndServer.startClientAndServer(port)
        ObjectMapperProvider.configure(DatabindCodec.mapper());
        def vertx = Vertx.vertx()
        slClient = new SlClient(vertx, "http://localhost:" + port, "abc")
    }

    def "should return list of Bus routes with stops"() {
        given:
        mockServer.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/LineData.json")
                        .withQueryStringParameters(
                                Parameter.param("model", "jour"),
                                Parameter.param("DefaultTransportModeCode", "BUS"),
                                Parameter.param("key", "abc")
                        )
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody(this.getClass().getResource('/busLines.json').text)
        )

        when:
        List<SlLine> result
        slClient.allSlBusesWithStopIds.onComplete {
            result = it.result()
        }

        then:
        Awaitility.await().atMost(Duration.ofMillis(500)).until {
            result != null &&
                    result.collect { it.lineNumber() }.unique() == ["1"]
        }

    }

    def "should throw HttpException if sl api to list Bus lines returns 404"() {
        given:
        mockServer.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/LineData.json")
                        .withQueryStringParameters(
                                Parameter.param("model", "jour"),
                                Parameter.param("DefaultTransportModeCode", "BUS"),
                                Parameter.param("key", "abc")
                        )
        ).respond(HttpResponse.response()
                .withStatusCode(404)
                .withBody("Not found")
        )

        when:
        Throwable result
        slClient.allSlBusesWithStopIds.onComplete {
            result = it.cause()
        }

        then:
        Awaitility.await().atMost(Duration.ofMillis(500)).until {
            result != null &&
                    result instanceof HttpException &&
                    ((HttpException) result).statusCode == 404 &&
                    ((HttpException) result).payload == "Not found"
        }
    }

    def "should throw HttpException if sl api to list Bus lines returns 500"() {
        given:
        mockServer.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/LineData.json")
                        .withQueryStringParameters(
                                Parameter.param("model", "jour"),
                                Parameter.param("DefaultTransportModeCode", "BUS"),
                                Parameter.param("key", "abc")
                        )
        ).respond(HttpResponse.response()
                .withStatusCode(500)
                .withBody("Internal Server error")
        )

        when:
        Throwable result
        slClient.allSlBusesWithStopIds.onComplete {
            result = it.cause()
        }

        then:
        Awaitility.await().atMost(Duration.ofMillis(500)).until {
            result != null &&
                    result instanceof HttpException &&
                    ((HttpException) result).statusCode == 500 &&
                    ((HttpException) result).payload == "Internal Server error"
        }
    }

    def "should return list of Bus stops"() {
        given:
        mockServer.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/LineData.json")
                        .withQueryStringParameters(
                                Parameter.param("model", "stop"),
                                Parameter.param("DefaultTransportModeCode", "BUS"),
                                Parameter.param("key", "abc")
                        )
        ).respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody(this.getClass().getResource('/busStops.json').text)
        )

        when:
        List<SlBusStop> result
        slClient.allSlBusStops.onComplete {
            result = it.result()
        }

        then:
        Awaitility.await().atMost(Duration.ofMillis(500)).until {
            result != null &&
                    result.collect { it.stopPointNumber() }.unique() == ["10001", "10002"]
        }

    }
}
