# stop
The stop command is used to stop a running server.
```
impulse stop <server>
```
This is useful for stopping a server that is running in the background or to stop a server that is no longer needed. This also works for servers with an `inactiveTimeout` of 0 that would otherwise never stop.

> [!NOTE]
> Although the implementation is broker specific, the semantic of this command is to *hault* a running server without removing of freeing its underlying resources. This in theory allows for a faster resume of the server. For example with the Docker broker, the container is stopped and put into the `exited` state. The container is not removed from the system.

### Specify a server
The server argument is the name of the server you would like to stop. This is the name you defined in the `servers` section of the `config.yaml` file.

### Examples
```
impulse stop smp
```

### Permission Scope
Use the `impulse.server.stop` permission scope to control who can use this command.
