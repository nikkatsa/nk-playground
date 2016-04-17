package com.nikoskatsanos.cli;

import org.junit.Test;

/**
 * @author nikkatsa
 */
public class TestPersonalInfoReader {

    @Test
    public void testShortCmdOptions() {
        PersonalInfoReader.main("-n", "John", "-s", "Doe", "-a", "20", "-e", "john@doe.com");
        PersonalInfoReader.main("-n", "John", "-s", "Doe", "-a", "20");
        PersonalInfoReader.main("-n", "John", "-s", "Doe", "-a", "20", "-h");
    }

    @Test
    public void testLongCmdOptions() {
        PersonalInfoReader.main("--name", "John", "--surname", "Doe", "--age", "20", "--email", "john@doe.com");
        PersonalInfoReader.main("--name", "John", "--surname", "Doe", "--age", "20");
        PersonalInfoReader.main("--name", "John", "--surname", "Doe", "--age", "20", "--help");
    }

    @Test(expected = RuntimeException.class)
    public void testWithMissingOptions() {
        PersonalInfoReader.main("-n", "John");
    }

    @Test(expected = RuntimeException.class)
    public void testWithWrongTypeParameter() {
        PersonalInfoReader.main("-n", "John", "-s", "Doe", "-a", "20a", "-e", "john@doe.com");
    }
}
