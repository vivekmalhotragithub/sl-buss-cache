# Description

The application has a mainClass Bootstrap.java. The technology stack used:
* Java 17
* vert.x
* groovy/spock for unit testing
* rest api

## SL API used
I used the following endpoints from SL api suit
* get all bus lines
  https://api.sl.se/api2/LineData.json?model=jour&DefaultTransportModeCode=BUS&key=<appKey>
* get all bus stops
  https://api.sl.se/api2/LineData.json?model=stop&DefaultTransportModeCode=BUS&key=<appKey>

## Application config
```yaml
slBaseUrl: https://api.sl.se/api2
refreshCacheInSeconds: 360
appKey: 5da196d47f8f4e5facdb68d2e25b9eae
port: 16000
```
You can control the refresh rate of sl bus lines using _refreshCacheInSeconds_ property.

## Rest api
the rest api is available 
```shell
http://localhost:16000/api/top10BusLines
```

## Running the application 

* in Intellij
run the application with run config .idea/runConfigurations/Application.xml

* as fat jar
```shell
mvn clean package
java -jar target/sl-buss-longest-1.0-SNAPSHOT.jar config/dev.yaml
```

