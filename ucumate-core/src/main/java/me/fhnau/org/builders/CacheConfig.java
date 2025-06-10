package me.fhnau.org.builders;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * @author Felix Naumann
 */
public interface CacheConfig {

    interface FinishStep {
        Properties build();
    }

    interface CustomPreHeatStep extends FinishStep {
        FinishStep overrideDefaultPreHeatCodes(String customPreHeatCodesFilename);
        FinishStep addToDefaultPreHeatCodes(String customPreHeatCodesFilename);
    }

    interface PreHeatStep extends FinishStep {
        CustomPreHeatStep preHeat(boolean preHeat);
    }

    interface RecordStatsStep extends FinishStep, PreHeatStep {
        PreHeatStep recordStats(boolean recordStats);
    }

    interface SizeStep extends FinishStep, RecordStatsStep, PreHeatStep {
        RecordStatsStep size(int maxSize);
        RecordStatsStep size(int maxValSize, int maxCanonSize);
    }

    interface EnableStep {
        SizeStep enable();
        FinishStep disable();
    }

    public static EnableStep builder() {
        return new Builder();
    }

    public static class Builder implements EnableStep, SizeStep, RecordStatsStep, PreHeatStep, CustomPreHeatStep, FinishStep {

        private Properties props = new Properties();

        @Override
        public SizeStep enable() {
            props.put("ucumate.cache.enable", true);
            return this;
        }

        @Override
        public FinishStep disable() {
            props.put("ucumate.cache.enable", false);
            return this;
        }

        @Override
        public PreHeatStep recordStats(boolean recordStats) {
            props.put("ucumate.cache.recordStats", recordStats);
            return this;
        }

        @Override
        public RecordStatsStep size(int maxSize) {
            return size(maxSize, maxSize);
        }

        @Override
        public RecordStatsStep size(int maxValSize, int maxCanonSize) {
            props.put("ucumate.cache.maxCanonSize", maxCanonSize);
            props.put("ucumate.cache.maxValSize", maxValSize);
            return this;
        }

        @Override
        public Properties build() {
            return props;
        }

        @Override
        public CustomPreHeatStep preHeat(boolean preHeat) {
            props.put("ucumate.cache.preheat", preHeat);
            return this;
        }

        @Override
        public FinishStep overrideDefaultPreHeatCodes(String customPreHeatCodesFilename) {
            props.put("ucumate.cache.preheat.override", true);
            props.put("ucumate.cache.preheat.codes", customPreHeatCodesFilename);
            return this;
        }

        @Override
        public FinishStep addToDefaultPreHeatCodes(String customPreHeatCodesFilename) {
            props.put("ucumate.cache.preheat.override", false);
            props.put("ucumate.cache.preheat.codes", customPreHeatCodesFilename);
            return this;
        }
    }
}
