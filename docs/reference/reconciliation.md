# Reconciliation

In Impulse, reconciliation is the process of ensuring that a servers configuration matches its running state. If they do
not then action is taken to bring them back into alignment. The exact behavior of reconciliation is broker specific, but
in general it will involve stopping the server, applying the new configuration, and restarting automatically. You can
control this behavior by setting the `reconciliationBehavior` and `reconciliationGracePeriod`  keys in the server
configuration.

## Reconciliation Behavior

### `FORCE`

When the reconciliation behavior is set to `FORCE` Impulse will immediately trigger a server restart to reconcile. The
players are given a configurable grace period to finish up before the server is stopped. Duting this time a warning
banner will be displayed to the players.

### `ON_STOP`

When the reconciliation behavior is set to `ON_STOP` Impulse will only reconcile the server when it naturally restarts,

## Reconciliation Grace Period

This is the amount of time in seconds that Impulse will wait before stopping a server during a reconciliation event.