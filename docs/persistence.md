---
order: 80
---
# Data Persistence

The `core` library ships with a default caffeine cache for validation and canonicalization results. On top of that it
is also possible to add another persistent data storage. If a persistent data storage is setup, then on startup all saved
codes will be read into the cache (if the cache is enabled).

Add the additional dependency:

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-persistence</artifactId>
    <version>v1.0.0</version>
</dependency>
```

The module defines 4 persistence providers out of the box:

* SQLite Provider
* PostgreSQL Provider
* MySQL Provider
* MongoDB Provider

By default, the SQLite provider is enabled out of the box as soon as the `ucumate-persistence` dependency is added.
If you want to use a different database option you can override the sqlite one with `PersistenceRegistry#register`.

```java
// Make sure you added the ucumate-persistence dependency
public static void main(String[] args) {
    // Doing nothing uses the sqlite implementation
    PersistenceRegistry.register("postgres", PersistenceProviderFactory.createPostgres(jdbcUrl, username, password));
    // now any reads or writes will happen to the postgres db
}
```

The library will write any encountered code
into the provided storage. On read, it first looks through the cache (if enabled) and only if it can't find it in the cache, it will query
the data storage for it. If it's not in there yet, then it will be calculated once and stored both in cache (if enabled)
and in the data storage.

You can also add your own persistence provider by implementing `PersistenceProvider` and registering it using `PersistenceRegistry#register`.
