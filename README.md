# gRPC Weather Reporter

A Java client-server application demonstrating gRPC and Protocol Buffers for weather data reporting.  
Includes both unary and server-side streaming RPCs.

---

## 🚀 Features

- **gRPC server** (`WeatherReporterServer`) that delivers weather data
- **gRPC client** (`WeatherClient`) that queries for weather data
- **Protocol Buffers** for API contract (`weather_reporter.proto`)
- **Unary RPC:** Get weather for a single city on a specific day
- **Server-side streaming RPC:** Get weather for a city over a date range
- **Easy to build and run with Gradle**

---

## 🛠️ Technologies

- Java 17
- gRPC
- Protocol Buffers
- Gradle

---

## 📄 Protocol Definition (`weather_reporter.proto`)

```protobuf
syntax = "proto3";

package weatherreporter;

message Weather {
  float temperature = 1;
  float humidity = 2;
  float wind = 3;
}

message Date {
  int32 day = 1;
  int32 month = 2;
  int32 year = 3;
}

message Location {
  string city = 1;
  string country = 2;
}

message
