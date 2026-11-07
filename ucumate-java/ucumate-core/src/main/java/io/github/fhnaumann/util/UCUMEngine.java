package io.github.fhnaumann.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UCUMEngine {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime()
        .availableProcessors());

    public static ExecutorService getExecutor() {
        return EXECUTOR;
    }

    public static void shutdownExecutor() {
        EXECUTOR.shutdown();
    }
}
