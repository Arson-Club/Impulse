# Configuration
Impulse exposes a lot of options so you can tweak its behavior to your liking. The configuration file is located at `plugins/impulse/config.yaml`.
This configuration is hot reloaded.

Below is a breakdown of the options available.

### Global Configuration
Impulse has a few global configuration options to be aware of. You can tweak these settings to adjust how Impulse behaves.
They are as follows:

| Key                         | Type      | Description                                                                                                                          | Default  |
|-----------------------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------|----------|
| `instanceName`              | `string`  | Used internally and by some ServerBrokers to uniquely identify this Impulse instance. Set this to something unique                   | Velocity |
| `serverMaintenanceInterval` | `integer` | The interval in seconds that impulse will use to schedule its periodic maintenance tasks. Normally you will not have to change this. | 300      |
| `messages`                  | `object`  | The messages that Impulse will send to players on various events. See the [Messages](#Messages) section for more information         |          |
| `servers`                   | `list`    | The configuration for each server managed by Impulse. See the [Server Configuration](#Server-Configuration) section for more info    |          |

### Messages
Impulse has a few messages that it will send to players on various events. These messages can be customized to your liking.
All messages support [MiniMessage](https://docs.adventure.kyori.net/minimessage.html) formatting.

| Key                       | Type       | Description                                                                                                                                                           | Default                                                                                                         |
|---------------------------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| `startupError`            | `string`   | Disconnect message sent to a player when Impulse fails to start a server on their behalf, or the connection timed out while starting.                                 | Server is starting, please try again in a Moment...<br/>If this error persists, please contact an administrator |
| `reconcileRestartTitle`   | `string`   | Title of on-screen title send to players when a server is being restarted due to a reconcile operation. See [Reconciliation](#Reconciliation) for more information    | Server is Restarting...                                                                                         |
| `reconcileRestartMessage` | `string`   | Subtitle of on-screen title send to players when a server is being restarted due to a reconcile operation. See [Reconciliation](#Reconciliation) for more information | server restart imminent                                                                                         |

### Server Configuration
This is where you will define the servers that Impulse will manage. Each server has its own configuration, and the keys are as follows:

| Key                               | Type      | Description                                                                                                                                                                                                                                              | Default |
|-----------------------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `name`                            | `string`  | *Required*: The name of the server. **Must match the server name in velocity.toml**                                                                                                                                                                      | NONE    |
| `type`                            | `enum`    | *Required*: One of (`docker`). Sets the server broker type.                                                                                                                                                                                              | NONE    |
| `inactiveTimeout`                 | `integer` | Amount of time in seconds to wait while server is empty before shutting down. If set to 0 server will never stop automatically                                                                                                                           | 0       |
| `startupTimeout`                  | `integer` | Amount of time in seconds to wait for a the server to start. If this will exceeded the client will be dropped with `startupError` but Impulse will still try and start the server in the background                                                      | 120     |
| `stopTimeout`                     | `integer` | Amount of time in seconds Impulse will wait to confirm a server has stopped successfully                                                                                                                                                                 | 120     |
| `forceServerReconciliation`       | `boolean` | If set to `true` Impulse will immediately trigger a server restart to reconcile configuration instead of waiting for the next time the server naturally restarts. This is only delayed by the `serverReconciliationGracePeriod`                          | false   |
| `serverReconciliationGracePeriod` | `integer` | Time in seconds to wait before stopping a server during a reconciliation event. While in the grace period existing and new clients will have the `reconcileRestartTitle` and `reconcileRestartMessage` displayed warning them of the restart             | 60      |
| `shutdownBehavior`                | `enum`    | One of `STOP` or `REMOVE`. General behavior for stopping the server. `STOP` should pause the server without deleting it while `REMOVE` will completely free any associated reources (besides volumes/user data). The exact behavior is broker specific   | `STOP`  |
| `docker`                          | `object`  | Object defining the Docker broker specific configuration. Only used when `type` is set to `docker`                                                                                                                                                       |         |

### Docker Configuration
Docker broker specific configuration values

| Key            | Type     | Description                                                                                                                   | Default                     |
|----------------|----------|-------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| `image`        | `string` | Docker image to use for the server                                                                                            | itzg/minecraft-server       |
| `portBindings` | `list`   | List of port bindings to use for the server. Each entry should be a string in the format `hostPort:containerPort`             | ["25565:25565"]             |
| `hostPath`     | `string` | URI to the docker daemon location. This can either be a local socket, or a remote host.                                       | unix:///var/run/docker.sock |
| `volumes`      | `Map`    | Map of host directories to mount into the container. The key is the host directory, and the value is the container directory. | {}                          |
| `env`          | `Map`    | Map of environment variables to pass to the container. The key is the variable name, and the value is the variable value.     | {"ONLINE_MODE": "false"}    |

