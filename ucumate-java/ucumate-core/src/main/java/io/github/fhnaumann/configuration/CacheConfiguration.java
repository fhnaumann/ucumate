package io.github.fhnaumann.configuration;

import java.util.Properties;

/**
 * @author Felix Naumann
 */
public record CacheConfiguration(boolean enable, int maxCanonSize, int maxValSize, boolean recordStats, boolean preheat, boolean preheatOverride, String preheatCodeFilename) {
    public static CacheConfiguration fromProps(Properties properties) {
        boolean enableCache = Boolean.parseBoolean(properties.getProperty("ucumate.cache.enable", "true"));
        int maxCanonSize = Integer.parseInt(properties.getProperty("ucumate.cache.maxCanonSize", "10000"));
        int maxValSize = Integer.parseInt(properties.getProperty("ucumate.cache.maxValSize", "10000"));
        boolean recordStats = Boolean.parseBoolean(properties.getProperty("ucumate.cache.recordStats", "false"));
        boolean preHeat = Boolean.parseBoolean(properties.getProperty("ucumate.cache.preheat", "true"));
        boolean overrideInsteadOfAdd = Boolean.parseBoolean(properties.getProperty("ucumate.cache.preheat.override", "false"));
        String preHeatCodesFilename = properties.getProperty("ucumate.cache.preheat.codes", "");
        return new CacheConfiguration(
                enableCache,
                maxCanonSize,
                maxValSize,
                recordStats,
                preHeat,
                overrideInsteadOfAdd,
                preHeatCodesFilename
        );
    }

    public Properties asProps() {
        Properties props = new Properties();
        props.put("ucumate.cache.enable", Boolean.toString(enable));
        props.put("ucumate.cache.maxCanonSize", Integer.toString(maxCanonSize));
        props.put("ucumate.cache.maxValSize", Integer.toString(maxValSize));
        props.put("ucumate.cache.recordStats", Boolean.toString(recordStats));
        props.put("ucumate.cache.preheat", Boolean.toString(preheat));
        props.put("ucumate.cache.preheat.override", Boolean.toString(preheatOverride));
        props.put("ucumate.cache.preheat.codes", preheatCodeFilename);
        return props;
    }
}
