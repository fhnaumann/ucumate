---
order: 90
---
# Cache

By default, the `core` library has a built-in cache that is enabled. Any expression that is encountered during validation
or canonicalization is being cached. If the cache is full then older entries will be removed.

If you enable preheating then you have to initialize the cache once on startup.
```java
PersistenceRegistry.initCache(props);
```

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