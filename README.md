# Impulse
Impulse is a plugin for the Minecraft server proxy [Velocity](https://papermc.io/software/velocity). It adds the ability
to dynamically start and stop servers on demand as players join and leave. Why run a server that is only used for a few
hours a day?

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg?style=for-the-badge)](https://github.com/Arson-Club/Impulse/blob/main/LICENSE.txt)
[![Release](https://img.shields.io/badge/dynamic/json?label=Release&query=tag_name&url=https://api.github.com/repos/Arson-Club/Impulse/releases/latest&style=for-the-badge)](https://github.com/Arson-Club/Impulse/releases)
[![Hangar](https://img.shields.io/badge/dynamic/json?color=004ee9&label=Hangar&query=name&url=https://hangar.papermc.io/api/v1/projects/Impulse&style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAACXBIWXMAAATrAAAE6wHYKlsNAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAA6hJREFUOI2FVG1MW2UYPe+9t71tmXRA5l2/pt1kbl3FmLFsaSXxAz+IQ81+4DSIjSYQN6MxzswgKmpC+LMQjVGbGTQuccagxi6LsX/kKw0VayVth64DypoFFYJj3vYWeu99/VHa9bqCz7/7nOec9zzve3MIpRSbVxsrpa++92moPjCUPr7/wUOej7fVrpDnXHS50jSzuRgh4szyqbPD2285f7HxVZvp7Xt1+lx4bWrrC8pjR4+gq0t3A2Mzh5emDr/52U+3NUjXFtmH5YRp92wdLyzVOPQK51QplfOy0mQ8PxSuKOj1endxHBckhLAFiBLCMDoCFaxKSgRKARW0OKGolE6NjIwcLuIcAODRth1P66uah1h1JyHXyQDA8zwsFgtSqVTBAQC2DM/n85r5giDDPnkHzzcPyZIGtFgs6Ovrg91uRzweRyAQwNjYGGRZ3vCamI/ipOF0e9CpJyxXDphMJvT398NutwMA3G43uru70dHRsaEYADCXZu9ks7vEg5Iup3md9vZ2WK1WzbDf78fg4KCmx7JEd3Wu+ZWS4C/JL19MxO5fmbMs/lNs6vV6tLS0aIjj4+PIZDIwGAyaPgUhn4fvaspdeehdAGBf79SZfr78uKt2+3fK7MXaPYQQuFwutLa2lkiKoiAWi8Hn80EQBEiShIWFhQKoKhllH5+ulhin1fjJbuapgxeafa7vVxPZ/aWVbTabxsX09DQ8Hg9GR0fhdrvR29sLjlt/TxVopGP6gcgzzMIVEmRMdnPnA+7UZQt7s3r9XliN4Pz8PKqrqyGKIgRBgMFgQE1NzfrKQMPMVtTWfSN1hj6oZ4CvlGpn08sHhN9+LQosLS1pBGVZRjQaRSgUQjQaBaUUuVyuhDtTgvnAobPWv6dXzqz/Km+pA6e93xqNxncAIJFIQJbl0lqEECSTSYTDYYTDYTgcDoiiWHJYl9lSn11e+/rZlxr5iuEgiiJ6enowMTEBSinMZjOy2WwJT6fTKM8AVcWWkwNt7x9zUZGrJAgAkUgEkUgEgiDA4/EgGAxWnKOAsqYox3Qm+hegDQc3z/MxhvmfRPtPybI8Pzw8fGvxu+SQUjqTz+cbu41Vj+zhjF49Q9g/TRkptfOP1eTton5y0Zd/YkecbLtpOfRGf+7HMsFs+QE35uGRo3sVlezzHz/Xdq2Kd09Ods3xcoPuNe8Xq/W23O9GR/DEZo43DFj/BeI6F/gwl+Hue/7U3SdH99py9xgdP5woJOLG9S+2F3zuid6QbAAAAABJRU5ErkJggg==)](https://hangar.papermc.io/ArsonClub/Impulse)
[![Modrinth](https://img.shields.io/badge/dynamic/json?labelColor=black&color=1bd96a&label=Modrinth&query=title&url=https://api.modrinth.com/v2/project/UDyKMCWP&style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAAIGNIUk0AAHomAACAhAAA+gAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAJPUExURQAAABvZahWnUha1WAYzGQlHIxvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZav///9ScwmYAAADDdFJOUwAAAAAAAA8zW3uOYwIBK3rB6Pn+ml18KiGL5HEDquOIH07R/UzKz2zu+uLHIibtafWkVCMNBRqg7/RQuT8EQbvT+5ETDBSU/NAgCSdZlcQKii7mtxJY5fF/7D1SRkB+EcWh4UilOOtPMdTCR1PqN969vmGDCw7G4DSSsAcGHrSPr3bds5CEwDKoXumcZdwcG4KmjayX32A79pjOqRjIFoXynVYVgPi6qxDnL78p2obVJFquNbGZCPdyvHPZ1yhuh8s+iRzcsrEAAAABYktHRMQUDBvhAAAAB3RJTUUH5wQXDwgZWDUtiQAAAqRJREFUOMttU/k7lFEU/k4L4hsxtNAnhRgiJZOiSfbI2oJpmRFCi2kV0aaNVLTIEpVISmnf97r/WO+534yZnqfzw73vOe+559xz7rmKIoVYvLx95vj6qUSqVBW3sGrwnxsQaAwSwcDz5i9g0wxPfmFIqJCyiEgLE4vDl7iD8PGlETotIqOQahlAdJTLA5sWY5JsbNzysHiihEBWViRqRDOlgyFmJVtMq5JWJ5vhvyZlLevrEmUILKl8PihtvYWcYt6QLmNwFjjEb4SSkZnlZLO5yJzcPBg35bODwZfPZxYwWbC5sKjYzwBUkssxSg1wKNsCtHUb89vTy3GwopBxDmcJ9YdDJYB1B9t2WmUtcbtkqt18Mxsp9irsezS2VEu+uka/mqUWSrFd2VsnhLEetoZ9TFv3HwCexdUdjBWi0aH4YTuEvtoPM3/EO1nnuTpEjD2qHIM54DhR0wmA5hZySzIntyknsbZyi8IB0tp07tRpBDKfgeWsdDjHDlFGRIjnl3O0t573gcMFUBeVS1gvlxB1dOZxv0pIu9IF01UDZRdjr1SuZQhxvbvnRrmsMePmrdsrGPRaKP8OLtmn9KM7A4N3xb8yNExUb5RlZt2btpZbXajzPoYkBaDKrtADp3HkYdloo56ndgx37UcHxSP0tIWBGH9cg1nx6TJFTNhS7eCfcA1PJ+GgDbLDs2GuX3V05Ohj0xYSBGumyq/yfApw4EWTRxPJqxQvIF6+ks9Gr0d4nN+8Nbhoi+NdJEwVSc6hVNvHOUvg+5qeDxoP3GQz6x8/qXLuZ6Op7SPy+gNTnydSELabk1Z8Mbs/hlr4dbobfUgxJMS3UdXza9H33jqnA3/OH41FCZ7/l7HW8vOXiWc9GvPf06D953/T2O/EP8HBNtcH0Zm/lqFNUgTAex4AAAAldEVYdGRhdGU6Y3JlYXRlADIwMjMtMDQtMjNUMTU6MDg6MjQrMDA6MDAE5dOaAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDIzLTA0LTIzVDE1OjA4OjI0KzAwOjAwdbhrJgAAACh0RVh0ZGF0ZTp0aW1lc3RhbXAAMjAyMy0wNC0yM1QxNTowODoyNSswMDowMITaQU0AAAAASUVORK5CYII=
)](https://modrinth.com/plugin/impulse-server-manager)

## Installation
In short, download our latest release from one of our sources and place it in your Velocity plugins folder. For more detailed instructions see our [Getting Started](https://arson.club/impulse/getting-started.html) guide.

Sources:
- [Modrinth](https://modrinth.com/plugin/impulse-server-manager)
- [Hangar](https://hangar.papermc.io/ArsonClub/Impulse)
- [GitHub Releases](https://github.com/Arson-Club/Impulse/releases)

## Quick Start
**For a more detailed guide, see our [Getting Started](https://arson.club/impulse/getting-started.html) guide.**
After installing Impulse, you will need to configure it to work with your server setup. In this getting started guide, we
will be setting up a simple SMP server suitable for a small community or group of friends. We will configure Velocity to
start up our server when a player connects, and shut it down after 5 minutes of no players being online.

### Step 1: Configure Velocity

### Step 2: Configure Impulse

### Step 3: Configure the MC Server

### Step 4: Connect

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
[05:19:04 INFO] [impulse]: Server smp has no players, stopping
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
