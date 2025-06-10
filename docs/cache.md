---
order: 90
---
# Cache

By default, the `core` library has a built-in cache that is enabled. Any expression that is encountered during validation
or canonicalization is being cached. If the cache is full then older entries will be removed.

You can configure the cache through three ways:

### Property File

Create a `ucumate.properties` file in `src/main/resources`, i.e.

```properties
# Inside a file called ucumate.properties
ucumate.cache.enable=true
ucumate.cache.maxCanonSize=10000
ucumate.cache.maxValSize=10000
ucumate.cache.recordStats=false
ucumate.cache.preheat=true
ucumate.cache.preheat.override=false
```
### Properties in Java Code

Create a `Properties` object and call `initCache`.

```java
Properties props = new Properties();
props.put("ucumate.cache.enable", true);
props.put("ucumate.cache.maxCanonSize", 10000);
props.put("ucumate.cache.maxValSize", 10000);
props.put("ucumate.cache.recordStats", false);
props.put("ucumate.cache.preheat", true);
props.put("ucumate.cache.preheat.override", false);

PersistenceRegistry.initCache(props);
```

### Builder Class

There is a fluent builder class to construct cache settings.

```java
Properties props = CacheConfig.builder()
        .enable()
        .size(10000, 10000)
        .recordStats(false)
        .preHeat(true)
        .build();

PersistenceRegistry.initCache(props);
```

If you want save the cache between restarts you can add the `ucumate-persistence` [module](persistence.md) that ships with a sqlite database
by default. When the app starts everything from the data storage will be loaded into the cache.

Below are the properties to control the cache behaviour.

| Property Name                  | Default Value  | Description                                                                    |
|--------------------------------|----------------|--------------------------------------------------------------------------------|
| ucumate.cache.enable           | true           | Enables/Disables Cache                                                         |
| ucumate.cache.maxCanonSize     | 10000          | Max size for Canonical Cache                                                   |
| ucumate.cache.maxValSize       | 10000          | Max size for Validation Cache                                                  |
| ucumate.cache.recordStats      | false          | Enables/Disables Caffeine Stats Tracking                                       |
| ucumate.cache.preheat          | false          | Enables/Disables Preheating the Cache with Common Codes                        |
| ucumate.cache.preheat.override | false          | Enables/Disables Overriding default Common Codes List with A Custom Codes List |
| ucumate.cache.preheat.codes    | \<empty list\> | A List of Common Codes to be used for Preheating                               |