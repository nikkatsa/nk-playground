package com.nikoskatsanos.oo.design.patterns.builder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author nikkatsa
 */
public class PersonTest {

    @Test
    public void testPersonConstruction() {
        final Person person = Person.newBuilder().withName("John").withSurname("Doe").withAge(10).withEmail("john.doe@gmail.com").build();
        assertNotNull(person);
        assertEquals("John", person.getName());
        assertEquals("Doe", person.getSurname());
        assertEquals(10, person.getAge());
        assertEquals("john.doe@gmail.com", person.getEmail());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPersonConstruction_withInvalidAge() {
        Person.newBuilder().withName("John").withSurname("Doe").withAge(-10).withEmail("john.doe@gmail.com").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPersonConstruction_withInvalidEmail() {
        Person.newBuilder().withName("John").withSurname("Doe").withAge(10).withEmail("john.doe@gmail").build();
    }

    @Test(expected = NullPointerException.class)
    public void testPersonConstruction_withInvalidName() {
        Person.newBuilder().withSurname("Doe").withAge(10).withEmail("john.doe@gmail.com").build();
    }

    @Test(expected = NullPointerException.class)
    public void testPersonConstruction_withInvalidSurname() {
        Person.newBuilder().withName("John").withAge(10).withEmail("john.doe@gmail.com").build();
    }

    @Test(expected = NullPointerException.class)
    public void testPersonConstruction_withNullEmail() {
        Person.newBuilder().withName("John").withSurname("Doe").withAge(10).build();
    }
}
