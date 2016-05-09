package com.nikoskatsanos.oo.design.patterns.builder;

import com.nikoskatsanos.nkjutils.yalf.YalfLogger;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <p>Simple example showing the Builder Design Pattern <a href="https://en.wikipedia.org/wiki/Builder_pattern">https://en.wikipedia.org/wiki/Builder_pattern</a></p>
 *
 * @author nikkatsa
 */
public class Person {

    private static final YalfLogger log = YalfLogger.getLogger(Person.class);

    private final String name;
    private final String surname;
    private final int age;
    private final String email;

    private Person(String name, String surname, int age, String email) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.email = email;
    }

    public final String getName() {
        return name;
    }

    public final String getSurname() {
        return surname;
    }

    public final int getAge() {
        return age;
    }

    public final String getEmail() {
        return email;
    }

    public static PersonBuilder newBuilder() {
        return new PersonBuilder();
    }

    public static class PersonBuilder {

        private static final Pattern EMAIL_VALIDATION_REGEX = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

        private String name;
        private String surname;
        private int age;
        private String email;

        private PersonBuilder() {
        }

        public final PersonBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        public final PersonBuilder withSurname(final String surname) {
            this.surname = surname;
            return this;
        }

        public final PersonBuilder withAge(final int age) {
            this.age = age;
            return this;
        }

        public final PersonBuilder withEmail(final String email) {
            this.email = email;
            return this;
        }

        public final Person build() {
            this.validate();
            return new Person(this.name, this.surname, this.age, this.email);
        }

        private void validate() {
            Objects.requireNonNull(this.name, "The person should have a name");
            Objects.requireNonNull(this.surname, "The person should have a surname");
            Objects.requireNonNull(this.email, "The person should have an email");

            if (this.age <= 0) {
                throw new IllegalArgumentException(String.format("Age should be a positive integer"));
            }

            if (!EMAIL_VALIDATION_REGEX.matcher(this.email).matches()) {
                throw new IllegalArgumentException("Invalid email");
            }
        }
    }
}
