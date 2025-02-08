# pin

The `pin` command is used to ensure that a server is not stopped or removed by Impulse.

```
impulse pin <server>
```

This is useful for servers that you want to prevent auto shutdown on temporarily. For long term static servers, use the
`allowAutoStop` setting in the server configuration.

### Specify a server

The server argument is the name of the server you would like to start. This is the name you defined in the `servers`
section of the `config.yaml` file.

### Examples

```
impulse pin smp
```

### Permission Scope

Use the `impulse.server.pin` permission scope to control who can use this command.
