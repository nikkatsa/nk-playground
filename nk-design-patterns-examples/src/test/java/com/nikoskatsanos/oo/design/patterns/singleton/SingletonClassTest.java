package com.nikoskatsanos.oo.design.patterns.singleton;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author nikkatsa
 */
public class SingletonClassTest {

    @Test
    public void testSingletonClass() {
        final SingletonClass instanceI = SingletonClass.getInstance();
        final SingletonClass instanceII = SingletonClass.getInstance();

        assertTrue(instanceI == instanceII);
        assertEquals(instanceI, instanceII);
    }
}
