# Impulse
Impulse is a plugin for the Minecraft server proxy [Velocity](https://papermc.io/software/velocity). It adds the ability
to dynamically start and stop servers on demand as players join and leave. Why run a server that is only used for a few
hours a day?

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
![Static Badge](https://img.shields.io/badge/Release-v0.1.0-blue?link=https%3A%2F%2Fgithub.com%2FArson-Club%2FImpulse%2Freleases)
![Static Badge](https://img.shields.io/badge/Get%20It%20On%20Hangar-8A2BE2?link=https%3A%2F%2Fhangar.papermc.io%2FArson-Club%2FImpulse)

## Installation
To get started with Impulse, you will need to download our latest release from either [Hanger](https://hangar.papermc.io/ArsonClub/Impulse)
or our [Release Page](https://github.com/Arson-Club/Impulse/releases). Once downloaded place the jar file in your Velocity plugins folder.

## Getting Started
After installing Impulse, you will need to configure it to work with your server setup. In this getting started guide, we
will be setting up a simple SMP server suitable for a small community or group of friends. We will configure Velocity to
start up our server when a player connects, and shut it down after 5 minutes of no players being online.

### Prerequisites
#### Docker
We will be using the Docker broker for this example. This is currently the only broker available, but more are planned.
You can find instructions on how to install Docker [here](https://docs.docker.com/get-docker/). In this example we will
be running the server on the same machine as the proxy. For more advanced setups and options see the [Docker Broker](#Docker-Broker).

#### Velocity
You will need a working Velocity server to use Impulse. You can find instructions on how to install Velocity [here](https://docs.papermc.io/velocity).
We will touch on relevant configuration options in this guide, but it is **not a replacement** for the official documentation.
Make sure you are familiar with setting up Velocity before continuing.

### Step 1: Configuring Velocity (`velocity.toml`)
Create the servers as normal in the Velocity config. Make a note of the server names. Impulse will only manage a server if
its config name **matches exactly**.

For our example, we can use this basic server configuration:
```toml
[servers]
# Our smp server Impulse will run on the same server as the proxy
smp = "127.0.0.1:25566"
```

While we are here lets also set up modern forwarding (this is not required for Impulse):
```toml
player-info-forwarding = "modern"
```
You will also want to copy the string in the `forwarding.secret` file for later.

### Step 2: Configure Impulse (`plugins/Impulse/config.yaml`)
Impulse requires additional configuration. The configuration file is located at `plugins/impulse/config.yaml`.
In general, you will use this file to tell Impulse how to manage your server.

For this example we are using the path `/srv/smp` to hold the data of the server, but you can change this to whatever you
like. The following config will start a server on port 25566 when a player connects to the `smp` server in Velocity. The server
will then shut down after 5 minutes of inactivity.
```yaml
instanceName: MyCoolSMP
servers:
  - name: smp # Name matching the server name in velocity.toml
    inactiveTimeout: 300 # Stop the server after 5 minutes of inactivity
    type: docker # Server broker type
    docker:
      portBindings:
        - "25566:25565" # Tell Impulse what ports to bind to this server
      env:
        ONLINE_MODE: "FALSE" # Set the server to offline mode needed for Velocity
        TYPE: "FABRIC" # Set the server type to Fabric
        EULA: "TRUE" # Accept the EULA
        MODRINTH_PROJECTS: "fabricproxy-lite" # Automatically installs mod needed to use Velocity modern forwarding
      volumes:
        "/srv/smp": "/data" # Mount a directory from the host into /data for persistence, this will have your world data in it
```

### Step 3: Configure the Server (`/srv/smp/config/FabricProxy-Lite.toml`)
This is only needed if you are setting up modern forwarding. To inject the velocity secret we can create a file in the
data directory that Impulse will mount into the server. Add the following to the `FabricProxy-Lite.toml`:
```toml
hackOnlineMode = true
hackEarlySend = false
hackMessageChain = true
disconnectMessage = "This server requires you to connect with Velocity."
secret = "<YOUR SECRET FROM forwarding.secret HERE>"
```

### Step 4: Configure Docker
No specific docker configuration is needed for this example. Impulse will use the default Docker image `itzg/minecraft-server`.
Although we will attempt to pull the image automatically, you will get better performance if you pre-pull the image manually.
Simply run `docker pull itzg/minecraft-server` on the host machine.

### Step 5: Starting Velocity and Connecting
Start your Velocity server as normal. If everything is configured correctly, you should see a few log messages from Impulse:
```
[18:52:15 INFO] [Impulse]: Initializing ogso-velocity
[18:52:15 INFO] [Impulse]: Configuration reloaded
[18:52:16 INFO] [Impulse]: ServerManager: server smp reconciled
```
At this point Impulse is ready to go. Connect to your server and you should see the server start up in the background.
You can confirm this by running `docker ps -a` to list all your running containers.

After you disconnect from the server, you should see the server stop after 5 minutes of inactivity. You will see a log
message like:
```
[05:19:04 INFO] [impulse]: Server minigames has no players, stopping
```

Thats it! The rest of this README will cover the additional feature of Impulse as well as all the configuration options.
I recommend you give it a read to get the most out of Impulse, and tweak your setup to your liking.

## Configuration
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

## Features
### Commands
Impulse adds a few commands to the proxy to help you manage your servers. These have an associated permission scope under `impulse.*`

| Command                            | Description                                                                                     | Permission                 |
|------------------------------------|-------------------------------------------------------------------------------------------------|----------------------------|
| `/impulse warm <server>`           | Starts a server that is not currently running                                                   | `impulse.server.warm`      |
| `/impulse remove <server>`         | Stops and removes a running server                                                              | `impulse.server.remove`    |
| `/impulse stop <server>`           | Stops a running server                                                                          | `impulse.server.stop`      |
| `/impulse reconcile <server>`      | Reconciles a server with its configuration (reload)                                             | `impulse.server.reconcile` |
| `/impulse status Optional<server>` | Displays the status of a server. If no server is provided it displays the status of all servers | `impulse.server.status`    |

### Configuration Hot Reload and Reconciliation
Impulse supports hot reloading of its configuration. This even extends to the server configurations!

When a config change is detected a custom `ConfigReloadEvent` is fired that you can listen for in your own plugins. This
allows you to inject your own logic into the reload process from your plugins. You can even create "virtual" servers this
way that do not exist in the config file.

If a server's configuration is changed, Impulse will attempt to reconcile the running server to match the new configuration.
The exact behaviour of this is broker specific, but in general it will involve stopping the server, applying the new configuration,
and restarting automatically. You can control this behavior with the `forceServerReconciliation` and `reconciliationGracePeriod`
If `forceServerReconciliation` is set to `true` Impulse will immediately trigger a server reconciliation. If set to `false`
Impulse will only reconcile the server when it naturally restarts, such as after all players leave and the `inactiveTimeout`
is reached. If the reconciliation requires a server restart, the `reconciliationGracePeriod` is the amount of time in
seconds the Impulse will give players to finish up before the server is stopped. During this time a configurable message
will be displayed to all connected clients.

### Unmanaged Servers
Impulse plays nice with unmanaged servers. If there is no config block for a server in the `config.yaml` Impulse will
ignore if. This allows you to have a mix of managed and unmanaged servers on your proxy. If you ever do want to move an
unmanaged server over, simply add the config block and Impulse will adopt it without any downtime.

### Custom Events
Impulse fires a few custom events that you can listen for in your own plugins.

#### `ConfigReloadEvent`
This is fired whenever Impulse reloads its configuration. You can listen for this event to inject your own logic into the
resulting configuration.

## Brokers
Impulse uses the concept of "brokers" to manage connections to technologies that can run Minecraft servers. How exactly a
server is created, started, stopped, destroyed, and reconciled is up to the broker's implementation. Currently, Impulse
only supports a Docker broker, but more are already planned.

### Docker Broker
This broker connects directly to a Docker daemon. It handles most of the complex start/stop logic internally. Docker is
relatively easy to set up, while still allowing advanced deployments and architectures. This is the "default" broker.

During reconciliation, docker will not automatically restart the server if resources are removed, such as volumes or ports.
These should be removed on the next restart.

The docker broker is capable of connecting to a remote docker daemon. This is useful for advanced setups where you want
to run the servers on a different machine. You can find instructions on how to set up a remote docker daemon
[here](https://docs.docker.com/engine/daemon/remote-access/).

### [WIP] Kubernetes Broker
Kubernetes is an advanced orchestration platform for running containerized applications. This is for advanced and large
scale deployments. This broker is still a work in progress and not yet available.

## Planned Features
These are upcoming features that are planned for future releases. They are not guaranteed and may change.
- [ ] Kubernetes Broker for advanced deployments
- [ ] JAR Broker for very simple deployments
- [ ] Metrics and monitoring endpoints
- [ ] Refactor Brokers into Addon modules
- [ ] Add MDBook documentation and guides for different setups
- [ ] More configurable client messaging

## Getting Help
If you require help with Impulse, feel free to open an issue with the `support` tag. I will do my best to respond.

## Contributing
All contributions are welcome! If you have a feature you would like to see, or a bug you would like to fix, feel free to
open a pull request. If you think it is going to be a large change, feel free to open an issue or a discussion first for
feedback.

## License
Impulse is Licensed under the AGPL-3.0 License. You can find the full license text in the `LICENSE` file.
