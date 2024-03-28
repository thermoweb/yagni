package fr.yagni.core.dto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
    public static void main(String[] args) {
        PersonBuilder personBuilder = new PersonBuilder();
        Person buildedPerson = personBuilder
                .lastname("CHAUVEAU")
                .firstname("Romain")
                .withoutPhone()
                .build();
        System.out.println(buildedPerson);

        jsonToDto();
    }

    private static void jsonToDto() {
        ObjectMapper om = new ObjectMapper();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("request.json");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String text = reader.lines().collect(Collectors.joining("\n"));
            Map<String, Object> map = om.readValue(text, new TypeReference<>() {});
            System.out.println(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Builder
    public record Person(String lastname, String firstname, @Builder.Nullable String phone) {
    }

    @Builder
    public record Enterprise(String name, List<Person> employees) {

    }

    @Builder
    public record Parameterized<T>(String name, @Builder.Nullable Integer age, T type) {
    }

    @Builder
    public record Pair<K, V>(K key, @Builder.Nullable V value) {
    }

    @Builder
    public static class SimplePojo {
        private final String name;
        @Builder.Nullable
        private final String description;

        public SimplePojo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

}
