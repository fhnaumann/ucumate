package io.github.fhnaumann.util;

import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Felix Naumann
 */
public class LogUtil {

    private LogUtil() {
    }

    public static <T> T logAndThrow(Logger logger, String message, Object... args) throws RuntimeException {
        return logAndThrow(logger, RuntimeException.class, message, args);
    }

    public static <T> T logAndThrow(Logger logger, Throwable base, String message, Object... args) {
        if(args == null) {
            args = new Object[] {};
        }
        logger.error(message, args);
        throw new IllegalStateException(message, base);
    }

    public static <T> T logAndThrow(Logger logger, Class<? extends RuntimeException> exceptionClazz, String message, Object... args) throws RuntimeException {
        if(args == null) {
            args = new Object[] {};
        }
        logger.error(message, args);
        try {
            throw exceptionClazz.getConstructor(String.class).newInstance(String.format(message.replace("{}", "%s"), args));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
