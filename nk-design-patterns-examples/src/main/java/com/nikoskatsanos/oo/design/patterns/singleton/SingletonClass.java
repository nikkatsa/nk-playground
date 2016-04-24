package com.nikoskatsanos.oo.design.patterns.singleton;

/**
 * <p>Singleton design pattern (<a href="https://en.wikipedia.org/wiki/Singleton_pattern">https://en.wikipedia.org/wiki/Singleton_pattern</a>
 * ) example, using an inner class helper, which allows the class to be lazily initialized</p>
 *
 * @author nikkatsa
 */
public class SingletonClass {

    private static final com.nikoskatsanos.nkjutils.yalf.YalfLogger log = com.nikoskatsanos.nkjutils.yalf.YalfLogger.getLogger(SingletonClass.class);

    private SingletonClass() {
        log.info("Initializing %s", this.getClass().getName());
    }

    public static final SingletonClass getInstance() {
        return SingletonClassHelper.INSTANCE;
    }

    private static class SingletonClassHelper {
        private static final SingletonClass INSTANCE = new SingletonClass();
    }
}
