package com.sbab.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sbab.assignment.adapter.sl.SlClient;
import com.sbab.assignment.service.ApiService;
import com.sbab.assignment.service.SlJourCacheService;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Bootstrap {
    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Please provide the application config");
        }
        var configPath = args[0];
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Provided config '" + configPath + "' not found.");
        }

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            AppConfig appConfig = mapper.readValue(configFile, AppConfig.class);
            logger.info("Starting application with config: {}", appConfig);
            Vertx vertx = Vertx.vertx();
            var cacheService = new SlJourCacheService(new SlClient(vertx, appConfig.slBaseUrl(), appConfig.appKey()));
            vertx.deployVerticle(cacheService)
                    .flatMap(__ -> vertx.deployVerticle(new ApiService(cacheService, appConfig.port())));

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse config '" + configPath + "'.", e);
        }
    }
}
