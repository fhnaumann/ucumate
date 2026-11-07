package io.github.fhnaumann.util;

import java.util.ServiceLoader;

/**
 * @author Felix Naumann
 */
public class ReflectionUtil {

    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static <T> T loadService(Class<T> clazz, T fallback) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        return loader.findFirst().orElse(fallback);
    }
}
