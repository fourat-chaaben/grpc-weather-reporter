package io.github.fouratchaaben.weatherreporter.grpc;

import io.github.fouratchaaben.weatherreporter.grpc.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * WeatherReporterServer
 * gRPC server implementation for serving weather data.
 */
public class WeatherReporterServer {

    private final int port;
    private final Server server;

    public WeatherReporterServer(int port, List<CityWeatherData> allWeatherData) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new WeatherReporterService(allWeatherData))
                .build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server");
            WeatherReporterServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Sample weather data for demonstration
        List<CityWeatherData> weatherDataList = new ArrayList<>();

        // Example sample data (replace with your own data or load from file)
        weatherDataList.add(CityWeatherData.newBuilder()
                .setLocationDate(LocationDate.newBuilder()
                        .setLocation(Location.newBuilder().setCity("Berlin").setCountry("Germany").build())
                        .setDate(Date.newBuilder().setDay(20).setMonth(6).setYear(2024).build())
                        .build())
                .setWeather(Weather.newBuilder().setTemperature(22.5f).setHumidity(60f).setWind(10f).build())
                .build());

        weatherDataList.add(CityWeatherData.newBuilder()
                .setLocationDate(LocationDate.newBuilder()
                        .setLocation(Location.newBuilder().setCity("Berlin").setCountry("Germany").build())
                        .setDate(Date.newBuilder().setDay(21).setMonth(6).setYear(2024).build())
                        .build())
                .setWeather(Weather.newBuilder().setTemperature(24.0f).setHumidity(55f).setWind(12f).build())
                .build());

        weatherDataList.add(CityWeatherData.newBuilder()
                .setLocationDate(LocationDate.newBuilder()
                        .setLocation(Location.newBuilder().setCity("Munich").setCountry("Germany").build())
                        .setDate(Date.newBuilder().setDay(20).setMonth(6).setYear(2024).build())
                        .build())
                .setWeather(Weather.newBuilder().setTemperature(20.0f).setHumidity(65f).setWind(8f).build())
                .build());

        WeatherReporterServer server = new WeatherReporterServer(50051, weatherDataList);
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * Implementation of the gRPC WeatherReporterService.
     */
    static class WeatherReporterService extends WeatherReporterGrpc.WeatherReporterImplBase {
        private final List<CityWeatherData> allWeatherData;

        public WeatherReporterService(List<CityWeatherData> allWeatherData) {
            this.allWeatherData = allWeatherData;
        }

        @Override
        public void getCityWeatherSingleDay(LocationDate request, StreamObserver<CityWeatherData> responseObserver) {
            for (CityWeatherData cityWeather : allWeatherData) {
                if (cityWeather.getLocationDate().equals(request)) {
                    responseObserver.onNext(cityWeather);
                    responseObserver.onCompleted();
                    return;
                }
            }
            // If not found, just complete with no result
            responseObserver.onCompleted();
        }

        @Override
        public void getCityWeatherMultipleDays(LocationDatePeriod request, StreamObserver<CityWeatherData> responseObserver) {
            for (CityWeatherData cityWeather : allWeatherData) {
                if (isInDatePeriod(cityWeather.getLocationDate(), request)) {
                    responseObserver.onNext(cityWeather);
                }
            }
            responseObserver.onCompleted();
        }

        /**
         * Checks if the given locationDate is within the period defined by LocationDatePeriod.
         */
        private boolean isInDatePeriod(LocationDate locationDate, LocationDatePeriod period) {
            // Match location (city & country)
            boolean sameCity = locationDate.getLocation().getCity().equalsIgnoreCase(period.getLocation().getCity());
            boolean sameCountry = locationDate.getLocation().getCountry().equalsIgnoreCase(period.getLocation().getCountry());
            if (!sameCity || !sameCountry) return false;

            // Build LocalDate objects for range check
            LocalDate target = LocalDate.of(
                    locationDate.getDate().getYear(),
                    locationDate.getDate().getMonth(),
                    locationDate.getDate().getDay());

            LocalDate start = LocalDate.of(
                    period.getStartDate().getYear(),
                    period.getStartDate().getMonth(),
                    period.getStartDate().getDay());

            LocalDate end = LocalDate.of(
                    period.getEndDate().getYear(),
                    period.getEndDate().getMonth(),
                    period.getEndDate().getDay());

            return (!target.isBefore(start)) && (!target.isAfter(end));
        }
    }
}
