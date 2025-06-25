# Managing UCUM Versions

`ucumate` currently supports two UCUM versions: `2.1` and `2.2`. The default version is `2.2` but you can change that
via the properties. Only one version is supported at runtime. So if you use the Java initialization, make sure to put
it at the very start of your program, or it has no effect.

!!!info Multiple Version Runtime Support

Please [get in contact](https://github.com/fhnaumann/ucumate/issues/new) if you need support for multiple versions at runtime.

!!!

```properties
# inside ucumate.properties
ucumate.ucumVersion=2.2
```

```java
ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion("2.2").build());
```