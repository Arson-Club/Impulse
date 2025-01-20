# remove
The remove command is used to stop a server and free its underlying resources.
```
impulse remove <server>
```
This is useful for making sure that the servers resources are completely freed up and that the server is no longer running. This command also works for servers with an `inactiveTimeout` of 0 that would otherwise never stop. The tradeoff is slower startup times as compared with the `stop` command.

> [!NOTE]
> The semantic of this command is to completely remove any underlying resources associated with the server. This effectively cleans up and frees any CPU or memory that was reserved for the server. Remove will neither delete the server from the `config.yaml` **nor remove any volume mounts or other persistent data**.

> [!WARNING]
> If using the Docker broker without a volume mount for the data directory (normally `/data`), all data will be lost when the server is removed. Be sure to use a volume mount to persist data across server removals if you want to keep your world.

### Specify a server
The server argument is the name of the server you would like to remove. This is the name you defined in the `servers` section of the `config.yaml` file.

### Examples
```
impulse remove smp
```

### Permission Scope
Use the `impulse.server.remove` permission scope to control who can use this command.
