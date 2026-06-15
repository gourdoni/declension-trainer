package io.github.gourdoni.declension.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

final class JsonHandler {

    private static final Gson INSTANCE = new GsonBuilder()
            .serializeNulls()
            // Handle the conversion of dates into ISO strings.
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private JsonHandler() {}

    static Gson instance() {
        return INSTANCE;
    }

    private static final class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value == null ? null : value.toString());
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString());
        }
    }
}
