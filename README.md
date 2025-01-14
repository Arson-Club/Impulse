# Impulse
Impulse is a plugin for the Minecraft server proxy [Velocity](https://papermc.io/software/velocity). It adds the ability
to dynmically start and stop servers on demand as players join and leave. Why pay for a server that is only used for a few
hours a day? Impulse allows you to only pay for the time your server is actually being used.

## Installation
To get started with Impulse, you will need to download our latest release from either [Hanger](https://hangar.papermc.io/ArsonClub/Impulse)
or our [Release Page](https://github.com/ArsonClub/Impulse/releases). Once downloaded place the jar file in your Velocity plugins folder.

## Getting Started
Once you have Impulse installed, you will need to configure it to work with your server setup. In this getting started guide,
we will be setting up a simple lobby server and a minigames server. The lobby server will be running on the same machine as
the proxy, and the minigames server will be running on a separate machine.

We will be using the Docker broker. As a prerequisite you must have Docker installed on all machines that
will be running servers. You can find instructions on how to install Docker [here](https://docs.docker.com/get-docker/).
If you wish to run a server on a different machine than the proxy, you will also need to configure remote access for the
Docker daemon. Some basic instructions are available [here](https://docs.docker.com/engine/daemon/remote-access/).

### Velocity Configuration (`velocity.toml`)
Most of this configuration is not related to Impulse, but special attention should be paid to the `servers` section. The
server names **must match the names in the Impulse configuration file exactly**. If they do not, Impulse will treat it as an
"unmanaged" server and will not attempt to interact with it.

Additionally, the server address should be the address of the machine that Impulse is configured to run the server on. For
our example, we can use this basic server configuration:

```toml
[servers]
# Our lobby server running locally on port 25566
lobby = "127.0.0.1:25566"
# Our minigames server running on a separate machine
minigames = "10.0.0.10:25565"
```

### Impulse Configuration (`plugins/Impulse/config.yaml`)
Impulse requires some additional configuration to work. The configuration file is located at `plugins/Impulse/config.yaml`.
For this example we can use the following configuration:

```yaml
instanceName: MyFirstImpulseInstance
servers:
  - name: lobby # Name matching the server name in velocity.toml
    type: docker # Server broker type
    docker:
      portBindings:
        - "25566:25565" # Tell Impulse what ports to bind to this server
      volumes:
        "/tmp/lobby": "/data" # Mount a directory from the host into /data for persistence
  - name: minigames
    type: docker
    docker:
      inactiveTimeout: 30 # Tell Impulse that we want to stop the server after 30 seconds of inactivity
      serverReconciliationGracePeriod: 10 # Tell Impulse to wait 10 seconds before stopping the server during a reconcile event
      volumes:
        "/tmp/minigames": "/data"
      hostPath: "tcp://10.0.0.10:2375" # Tell Impulse to connect to the Docker daemon on the minigames server"
```

### Starting Impulse and Connecting
For the first time setup, you will need to restart the proxy. If your configuration is correct, you should see a few log
messages.
```
[18:52:15 INFO] [Impulse]: Initializing ogso-velocity
[18:52:15 INFO] [Impulse]: Configuration reloaded
[18:52:16 INFO] [Impulse]: ServerManager: server lobby reconciled
[18:52:16 INFO] [Impulse]: ServerManager: server minigames reconciled
```
At this point impulse should be working as expected, the next few steps are a tour of the features of Impulse.

#### Joining the Lobby
Open your minecraft client and connect to the proxy. You may notice that you spend some time in the "Joining World" screen.
This is because Impulse is starting the server for you. Once the server is started, you will be connected to the lobby server.
Subsequent connections will be much faster as the server will already be running.

#### Connection to Minigames
Once you are in the lobby go ahead and run `/server minigames`. Again you will notice that the command takes a little longer
than normal to execute. Once the server is started in the background, you will be automatically transferred to the minigames
server. Anyone else who runs the command after will be transferred instantly.

#### Inactivity Timeout
Transfer back to the lobby server and wait for ~40 seconds. If you check the host running the minigames server, you will see
that the container is in the "exited" state as it has been automatically stopped by Impulse!

#### Reconciliation
In short, if you change the configuration of a server managed by Impulse it will attempt to automatically update the running
server to match the defined configuration without a proxy restart. That being said most actions will require a restart of
the server that is being reconciled. For full details on reconciliation see the [Reconciliation](#Reconciliation) section.

To see this in action, again connect to the minigames server with `/server minigames`. Once connected, open the Impulse
configuration and modify the `minigames` config, for example:
```yaml
servers:
  # Other server config...
  - name: minigames
    type: docker
    docker:
      inactiveTimeout: 30
      serverReconciliationGracePeriod: 10
      volumes:
        "/tmp/minigames": "/data"
        "/tmp/extras": "/extras" # Add a new volume to trigger reconciliation
      hostPath: "tcp://10.0.0.10:2375"
```

Back in the game you should see a message on your screen that the server is restarting. When the server restarts you should
be automatically connected back to your last server (in this case the lobby). After a few moments, you should be able to
connect to the minigames server again.

## Configuration
Impulse exposes a lot of options so you can tweak its behavior to your liking. The configuration file is located at `Impulse/config.yaml`.
Whenever the file is modified, Impulse will automatically hot reload the configuration and apply the changes. Most of the time this means
you will not need to restart the proxy to apply changes. If server configuration is changed, it may trigger a [Reconciliation](#Reconciliation)
event and server restart.

Below is a breakdown of the configuration options available.

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

| Key                         | Type      | Description                                                                                                                                                                                                                                  | Default |
|-----------------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `name`                      | `string`  | *Required*: The name of the server. **Must match the server name in velocity.toml**                                                                                                                                                          | NONE    |
| `type`                      | `enum`    | *Required*: One of (`docker`). Sets the server broker type.                                                                                                                                                                                  | NONE    |
| `inactiveTimeout`           | `integer` | Amount of time in seconds to wait while server is empty before shutting down. If set to 0 server will never stop automatically                                                                                                               | 0       |
| `startupTimeout`            | `integer` | Amount of time in seconds to wait for a the server to start. If this will exceeded the client will be dropped with `startupError` but Impulse will still try and start the server in the background                                          | 120     |
| `stopTimeout`               | `integer` | Amount of time in seconds Impulse will wait to confirm a server has stopped successfully                                                                                                                                                     | 120     |
| `forceServerReconciliation` | `boolean` | If set to `true` Impulse will immediately trigger a server restart to reconcile configuration instead of waiting for the next time the server naturally restarts. This is only delayed by the `serverReconciliationGracePeriod`              | true    |
| `reconciliationGracePeriod` | `integer` | Time in seconds to wait before stopping a server during a reconciliation event. While in the grace period existing and new clients will have the `reconcileRestartTitle` and `reconcileRestartMessage` displayed warning them of the restart | 60      |
| `docker`                    | `object`  | Object defining the Docker broker specific configuration. Only used when `type` is set to `docker`                                                                                                                                           |         |

### Docker Configuration
Docker broker specific configuration values

| Key            | Type     | Description                                                                                                                   | Default                     |
|----------------|----------|-------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| `image`        | `string` | Docker image to use for the server                                                                                            | itzg/minecraft-server       |
| `portBindings` | `list`   | List of port bindings to use for the server. Each entry should be a string in the format `hostPort:containerPort`             | ["25565:25565"]             |
| `hostPath`     | `string` | URI to the docker daemon location. This can either be a local socket, or a remote host.                                       | unix:///var/run/docker.sock |
| `volumes`      | `Map`    | Map of host directories to mount into the container. The key is the host directory, and the value is the container directory. | {}                          |
| `env`          | `Map`    | Map of environment variables to pass to the container. The key is the variable name, and the value is the variable value.     | {"ONLINE_MODE": "false"}    |

## Reconciliation
When server configuration is changed, Impulse will attempt to reconcile the running server to match the new configuration.
The exact behaviour of the reconciliation process is broker specific, but in general it will involve stopping the server,
When reconciliation is allowed to occur can be controlled by the `forceServerReconciliation` and `reconciliationGracePeriod` options.
`forceServerReconciliation` will Immediately trigger a server to reconcile, after displaying a "server restarting" message
to the connected clients for the given `reconciliationGracePeriod`. If `forceServerReconciliation` is set to `false` Impulse
will only reconcile the server when it naturally restarts, such as after all players leave and the `inactiveTimeout` is reached
or the proxy is restarted.

In general, you should avoid queueing reconciliation events for too long. This can lead to a server suddenly becoming
unavailable or other instability.

## Brokers
Impulse uses the concept of "brokers" to manage connections to technologies that can run Minecraft servers. How exactly a
server is created, started, stopped, destroyed, and reconciled is up to the broker's implementation. Currently, Impulse
only supports a Docker broker, but more are already planned.

### Docker Broker
This broker connects directly to a Docker daemon. It handles most of the complex start/stop logic internally. Docker is
relatively easy to set up, while still allowing advanced deployments and architectures. This is the "default" broker.

During reconciliation, docker will not automatically restart the server if resources are removed, such as volumes or ports.
These should be removed on the next restart.

### [WIP] Kubernetes Broker
Kubernetes is an advanced orchestration platform for running containerized applications. This is for advanced and large
scale deployments. This broker is still a work in progress and not yet available.

## Planned Features
- [ ] Kubernetes Broker for advanced deployments
- [ ] JAR Broker for very simple deployments
- [ ] Metrics and monitoring endpoints
- [ ] Refactor Brokers into Addon modules
- [ ] Add MDBook documentation and guides
- [ ] More configurable client messaging

## Outstanding Tasks for Release
- [x] ~~Add player count reconciliation and shutdown scheduling~~
- [x] ~~change banner logic slightly~~
- [x] ~~/warm command~~
- [x] ~~create config dir if missing~~
- [x] ~~add additional commands~~
- [x] ~~put commands under /impulse~~
- [x] ~~only restart previously running servers after reconciliation~~
- [x] ~~/status command~~
- [x] ~~pull image on demand~~
- [x] ~~env var forwarding for docker broker~~
- [x] ~~switch default server image~~
- [x] ~~add shutdown behavior~~
- [x] ~~env var reconciliation~~
- [x] ~~relocate dependencies~~
- [x] ~~/reconcile command~~
- [x] ~~write a better description~~
- [x] ~~reconcile on restart always causing downstream restart~~
- [x] ~~Fix checks to immediately reconcile if server is offline~~
- [x] ~~Add mechanism for offline reconciliation~~
- [ ] Finalize default configuration
- [ ] Final README updates