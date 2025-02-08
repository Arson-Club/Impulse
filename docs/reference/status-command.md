# status
The status command is used to check the status of a servers.
```
impulse status Optional<server>
```
If not provided with a server name, this command will return the status of all servers. If a server name is provided, it will return the status of that server only.

### Specify a server
The server argument is the name of the server you would like to check the status of. This is the name you defined in the `servers` section of the `config.yaml` file.

### Examples
Get the status of all servers:
```
impulse status
```

Get the status of a specific server:
```
impulse status smp
```

### Permission Scope
Use the `impulse.server.status` permission scope to control who can use this command.
