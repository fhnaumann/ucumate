---
order: 80
---
# Data Persistence

The `core` library ships with a default caffeine cache for validation and canonicalization results. On top of that it
is also possible to add another persistent data storage.

Add the additional dependency:
todo persistence module dependency.

The module defines 3 persistence providers out of the box:

* PostgreSQL Provider
* MySQL Provider
* MongoDB Provider

You need to register the provider once on startup with `PersistenceRegistry#register`. The library will write any encountered code
into the provided storage. On read, it first looks through the cache (if enabled) and only if it can't find it in the cache, it will query
the data storage for it. If it's not in there yet, then it will be calculated once and stored both in cache (if enabled)
and in the data storage.


You just need to provide the database connection details and everything should work out of the box.

You can also add your own persistence provider by implementing `PersistenceProvider` and registering it using `PersistenceRegistry#register`.
