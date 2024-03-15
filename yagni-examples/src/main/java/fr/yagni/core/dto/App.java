package fr.yagni.core.dto;

import java.util.List;

public class App {
    public static void main(String[] args) {
        PersonBuilder personBuilder = new PersonBuilder();
        Person buildedPerson = personBuilder
                .lastname("CHAUVEAU")
                .firstname("Romain")
                .withoutPhone()
                .build();
        System.out.println(buildedPerson);
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
