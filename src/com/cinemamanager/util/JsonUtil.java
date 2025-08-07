package com.cinemamanager.util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Supplier;

public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter()) // <--- NUEVO
            .create();

    public static <T> void write (String path, T data) {
        try (FileWriter writer = new FileWriter(path)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public static <T> T read (String path, Type typeOfT, Supplier <T> defaultSupplier) {
        File file = new File(path);
        if (!file.exists()) {
            return defaultSupplier.get();
        }
        try (FileReader reader = new FileReader(path)) {
            return GSON.fromJson(reader, typeOfT);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return defaultSupplier.get();
        }
    }

    private static class DurationAdapter extends TypeAdapter <Duration> {
        @Override
        public void write(JsonWriter out, Duration value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public Duration read(JsonReader in) throws IOException {
            return Duration.parse(in.nextString());
        }
    }

    private static class LocalTimeAdapter extends TypeAdapter <LocalTime> {
        @Override
        public void write(JsonWriter out, LocalTime value) throws IOException {
            out.value(value.toString());  // Ej: "14:30:00"
        }

        @Override
        public LocalTime read(JsonReader in) throws IOException {
            return LocalTime.parse(in.nextString());
        }
    }

}
