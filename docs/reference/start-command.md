# start
The start command is used to start a configured server without a client connecting.
```
impulse start <server>
```
This is useful for scripts or times where you would like to a server to start and prepare things before batch connecting clients (for instance a minigame instance).

### Specify a server
The server argument is the name of the server you would like to start. This is the name you defined in the `servers` section of the `config.yaml` file.

### Aliases
- `impulse warm`

### Examples
```
impulse start smp
```

### Permission Scope
Use the `impulse.server.warm` permission scope to control who can use this command.
