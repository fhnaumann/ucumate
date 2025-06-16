package io.github.fhnaumann.util;

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
}
