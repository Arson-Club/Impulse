# JAR Broker

The JAR broker is a specialization of the generic [command broker](command-broker.md). It is designed to give a better
config interface when starting java based minecraft servers. As such its capabilities and limitations are the same as
the command broker.

## Configuration

JAR broker specific configuration values. These should be nested under the `jar` key in the server configuration.

| Key                | Type           | Description                                                                                                     | Default |
|--------------------|----------------|-----------------------------------------------------------------------------------------------------------------|---------|
| `workingDirectory` | `string`       | Working directory to execute the jar in. This should probably be the root of your server data where the jar is. | `none`  |
| `jarFile`          | `string`       | Name of the jar file to run.                                                                                    | `none`  |
| `javaFlags`        | `list[string]` | Flags to apply to the java JVM instance.                                                                        | `[]`    |
| `flags`            | `list[string]` | Flags to apply to the JAR                                                                                       | `[]`    |
| `address`          | `string`       | Optional address of the server if using dynamic registration                                                    |         |