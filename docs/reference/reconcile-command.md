# reconcile
The reconcile command is used to force a server to reload its configuration and trigger a restart if needed.
```
impulse reconcile <server>
```
Although reconciliation normally happens automatically, this command can be helpful if you have `forceServerReconciliation` set to `false`. It will trigger the reconciliation immediately rather than waiting for a natural stop and start cycle. This is particularly helpful for long-lived lobbies.

> [!NOTE]
> The exact behavior of reconciliation is broker specific. In general, it will reload the configuration and restart or remove and recreate the server as needed.

### Specify a server
The server argument is the name of the server you would like to reconcile. This is the name you defined in the `servers` section of the `config.yaml` file.

### Examples
```
impulse reconcile smp
```

### Permission Scope
Use the `impulse.server.reconcile` permission scope to control who can use this command.
