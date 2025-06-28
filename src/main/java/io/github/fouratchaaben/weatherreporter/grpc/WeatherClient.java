package io.github.fouratchaaben.weatherreporter.grpc;

import io.github.fouratchaaben.weatherreporter.grpc.*;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * WeatherClient
 * A sample gRPC client for querying weather data from the WeatherReporterServer.
 */
public class WeatherClient {

    private static final Logger logger = Logger.getLogger(WeatherClient.class.getName());

    private final WeatherReporterGrpc.WeatherReporterBlockingStub blockingStub;

    /**
     * Constructs a client for accessing WeatherReporter server using the existing channel.
     */
    public WeatherClient(Channel channel) {
        blockingStub = WeatherReporterGrpc.newBlockingStub(channel);
    }

    /**
     * Blocking unary call example. Calls getCityWeatherSingleDay on the server and prints the response.
     */
    public void getCityWeatherSingleDay(int day, int month, int year, String city, String country) {
        Location location = Location.newBuilder()
                .setCity(city)
                .setCountry(country)
                .build();

        Date date = Date.newBuilder()
                .setDay(day)
                .setMonth(month)
                .setYear(year)
                .build();

        LocationDate request = LocationDate.newBuilder()
                .setLocation(location)
                .setDate(date)
                .build();

        logger.info("Requesting weather for " + city + ", " + country + " on " + day + "/" + month + "/" + year);

        CityWeatherData response = blockingStub.getCityWeatherSingleDay(request);
        if (response != null && response.hasWeather()) {
            logger.info("Weather: " + response.getWeather());
        } else {
            logger.info("No weather data found for this date/location.");
        }
    }

    /**
     * Blocking server-streaming call example.
     * Calls getCityWeatherMultipleDays and prints each CityWeatherData as it arrives.
     */
    public void getCityWeatherMultipleDays(
            int startDay, int startMonth, int startYear,
            int endDay, int endMonth, int endYear,
            String city, String country) {

        Location location = Location.newBuilder()
                .setCity(city)
                .setCountry(country)
                .build();

        Date startDate = Date.newBuilder()
                .setDay(startDay)
                .setMonth(startMonth)
                .setYear(startYear)
                .build();

        Date endDate = Date.newBuilder()
                .setDay(endDay)
                .setMonth(endMonth)
                .setYear(endYear)
                .build();

        LocationDatePeriod period = LocationDatePeriod.newBuilder()
                .setLocation(location)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .build();

        logger.info("Requesting weather for " + city + ", " + country +
                " from " + startDay + "/" + startMonth + "/" + startYear +
                " to " + endDay + "/" + endMonth + "/" + endYear);

        Iterator<CityWeatherData> responses = blockingStub.getCityWeatherMultipleDays(period);
        boolean found = false;
        while (responses.hasNext()) {
            CityWeatherData response = responses.next();
            if (response != null && response.hasWeather()) {
                logger.info("Weather: " + response.getWeather() +
                        " for " + response.getLocationDate().getDate());
                found = true;
            }
        }
        if (!found) {
            logger.info("No weather data found for this period/location.");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String target = "localhost:50051";
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [target]");
                System.err.println();
                System.err.println("  target  The server to connect to. Defaults to " + target);
                System.exit(1);
            }
            target = args[0];
        }

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        try {
            WeatherClient weatherClient = new WeatherClient(channel);

            // Example call: single day
            weatherClient.getCityWeatherSingleDay(20, 6, 2024, "Berlin", "Germany");

            // Example call: multiple days
            weatherClient.getCityWeatherMultipleDays(20, 6, 2024, 21, 6, 2024, "Berlin", "Germany");

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
