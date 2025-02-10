# Configuration

Impulse exposes a lot of options so you can tweak its behavior to your liking. The configuration file is located at
`plugins/impulse/config.yaml`. When a change is detected in the configuration file, Impulse will automatically reload
its configutaion and apply the changes. This even extends to the server configurations! Impulse will trigger
a [reconciliation](reconciliation.md) event for each server that has changed, keeping things up to date for you.

## Velocity Configuration

Impulse has two methods of exposing servers to Velocity, either through a static binding or a dynamic binding. The
difference between the two is outlined below. "Hot" reconciliation of binding is not supported but they can be refreshed
live by removing the server from the impulse configuration, waiting for reconciliation to complete, and then re-adding
the new server configuration.

### Static Binding

In this method you add your server configuration to the `velocity.toml` file as normal. Impulse will then adopt the
server reference in Velocity when it creates the server. This method mostly exists due to current limitations in the
Velocity API.

**When to Use**:

- You need to reference the server in the `try` block of Velocity's configuration
- You need to reference the server in the `forced-hosts` section of Velocity's configuration
- You want Velocity to always present the server in the server list, even if it is not in Impulse's configuration at all

### Dynamic Binding

If no server ref is found in the `velocity.toml` impulse will dynamically create one at runtime. Each broker will have
its own method of specifying the server address, so please refer to the appropriate broker documentation for more
information on how to configure this. For example with the Docker broker you would set the `address` key in its
configuration.

**When to Use**:

- You do not need anything mentioned in [Static Binding](#Static-Binding)
- Servers that will only be transferred to from another server (e.g. a mini games server only accessible from the lobby)

## Impulse Configuration Options

### Global Configuration

Impulse has a few global configuration options to be aware of. You can tweak these settings to adjust how Impulse
behaves.
They are as follows:

| Key                         | Type      | Description                                                                                                                          | Default  |
|-----------------------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------|----------|
| `instanceName`              | `string`  | Used internally and by some ServerBrokers to uniquely identify this Impulse instance. Set this to something unique                   | Velocity |
| `serverMaintenanceInterval` | `integer` | The interval in seconds that impulse will use to schedule its periodic maintenance tasks. Normally you will not have to change this. | 300      |
| `messages`                  | `object`  | The messages that Impulse will send to players on various events. See the [Messages](#Messages) section for more information         |          |
| `servers`                   | `list`    | The configuration for each server managed by Impulse. See the [Server Configuration](#Server-Configuration) section for more info    |          |

### Messages

Impulse has a few messages that it will send to players on various events. These messages can be customized to your
liking.
All messages support [MiniMessage](https://docs.adventure.kyori.net/minimessage.html) formatting.

| Key                       | Type     | Description                                                                                                                                                           | Default                                                                                                         |
|---------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| `startupError`            | `string` | Disconnect message sent to a player when Impulse fails to start a server on their behalf, or the connection timed out while starting.                                 | Server is starting, please try again in a Moment...<br/>If this error persists, please contact an administrator |
| `reconcileRestartTitle`   | `string` | Title of on-screen title send to players when a server is being restarted due to a reconcile operation. See [Reconciliation](#Reconciliation) for more information    | Server is Restarting...                                                                                         |
| `reconcileRestartMessage` | `string` | Subtitle of on-screen title send to players when a server is being restarted due to a reconcile operation. See [Reconciliation](#Reconciliation) for more information | server restart imminent                                                                                         |
| `autoStartDisabled`       | `string` | Message to send to a player when they try to start a server that has been disabled by an administrator                                                                | <red>Autostart is disabled for this server</red>                                                                |

### Server Configuration

This is where you will define the servers that Impulse will manage. Each server has its own configuration, and the keys
are as follows:

| Key                   | Type     | Description                                                                                                                            | Default |
|-----------------------|----------|----------------------------------------------------------------------------------------------------------------------------------------|---------|
| `name`                | `string` | *Required*: The name of the server. **Must match the server name in velocity.toml**                                                    | NONE    |
| `type`                | `enum`   | *Required*: One of (`docker`). Sets the server broker type.                                                                            | NONE    |
| `lifecycleSettings`   | `object` | Settings related to server start, stop, timeouts, and other lifecycle events                                                           |         |
| `BROKER_SPECIFIC_KEY` | `object` | Each broker will inject its own configuration key. For example, the Docker broker will inject a `docker` key to hold its configuration |         |

### Lifecycle Settings

| Key                      | Type      | Description                                                                                                                                                                                                                                                   | Default   |
|--------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| `timeouts`               | `object`  | Various internal timeouts and limits for the server                                                                                                                                                                                                           |           |
| `allowAutoStart`         | `boolean` | Is this server allowed to start automatically when the first player connects, or from any other trigger. Does not prevent manual start with `/impulse start`                                                                                                  | true      |
| `allowAutoStop`          | `boolean` | Is this server allowed to stop automatically when empty, or from any other automatic trigger. Does not prevent manual stops with `/impulse stop`                                                                                                              | true      |
| `reconciliationBehavior` | `enum`    | One of `FORCE` or `ON_STOP`. If set to `FORCE` Impulse will immediately trigger a server restart to reconcile configuration instead of waiting for the next time the server naturally restarts. This is only delayed by the `serverReconciliationGracePeriod` | `ON_STOP` |
| `shutdownBehavior`       | `enum`    | One of `STOP` or `REMOVE`. General behavior for stopping the server. `STOP` should pause the server without deleting it while `REMOVE` will completely free any associated reources (besides volumes/user data). The exact behavior is broker specific        | `STOP`    |

### Timeouts

| Key                         | Type      | Description                                                                                                                                                                                                                                  | Default |
|-----------------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `startup`                   | `integer` | Amount of time in seconds to wait for a the server to start. If this will exceeded the client will be dropped with `startupError` but Impulse will still try and start the server in the background                                          | 120     |
| `shutdown`                  | `integer` | Amount of time in seconds Impulse will wait to confirm a server has stopped successfully                                                                                                                                                     | 120     |
| `reconciliationGracePeriod` | `integer` | Time in seconds to wait before stopping a server during a reconciliation event. While in the grace period existing and new clients will have the `reconcileRestartTitle` and `reconcileRestartMessage` displayed warning them of the restart | 60      |
| `inactiveGracePeriod`       | `integer` | Amount of time in seconds to wait while server is empty before shutting down. If set to 0 server will never stop automatically                                                                                                               | 0       |

### Broker Specific Configuration

Each broker will inject its own configuration key into the server configuration. Please see the appropriate broker
documentation for more information on its configuration.

- [Docker Broker](./docker-broker.md)