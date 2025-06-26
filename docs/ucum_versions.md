# Managing UCUM Versions

`ucumate` currently supports two UCUM versions: `2.1` and `2.2`. The default version is `2.2` but you can change that
via the properties.

```properties
# inside ucumate.properties
ucumate.ucumVersion=2.2
```

```java
ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion("2.2").build());
```

You can also specify the desired version on a per-service basis. Explicitly providing a version will override the version 
specified in the properties.

```java
UCUMService defaultService = new UCUMService(); // uses what is in the `ucumate.ucumVersion` property
UCUMService service2_1 = new UCUMService("2.1");
UCUMService service2_2 = new UCUMService("2.2");
```