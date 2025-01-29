# Impulse

Impulse is a plugin for the Minecraft server proxy [Velocity](https://papermc.io/software/velocity). It adds the ability
to dynamically start and stop servers on demand as players join and leave. Why run a server that is only used for a few
hours a day?

<p style="text-align:center;">
    <a href="https://github.com/Arson-Club/Impulse/blob/main/LICENSE.txt"><img src="https://img.shields.io/badge/License-AGPL_v3-blue.svg?style=flat"></a>
    <a href="https://github.com/Arson-Club/Impulse/releases"><img src="https://img.shields.io/badge/dynamic/json?label=Release&query=tag_name&url=https://api.github.com/repos/Arson-Club/Impulse/releases/latest&style=flat"></a>
    <a href="https://hangar.papermc.io/ArsonClub/Impulse"><img src="https://img.shields.io/badge/dynamic/json?color=004ee9&labelColor=black&label=Hangar&query=name&url=https://hangar.papermc.io/api/v1/projects/Impulse&style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAACXBIWXMAAATrAAAE6wHYKlsNAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAA6hJREFUOI2FVG1MW2UYPe+9t71tmXRA5l2/pt1kbl3FmLFsaSXxAz+IQ81+4DSIjSYQN6MxzswgKmpC+LMQjVGbGTQuccagxi6LsX/kKw0VayVth64DypoFFYJj3vYWeu99/VHa9bqCz7/7nOec9zzve3MIpRSbVxsrpa++92moPjCUPr7/wUOej7fVrpDnXHS50jSzuRgh4szyqbPD2285f7HxVZvp7Xt1+lx4bWrrC8pjR4+gq0t3A2Mzh5emDr/52U+3NUjXFtmH5YRp92wdLyzVOPQK51QplfOy0mQ8PxSuKOj1endxHBckhLAFiBLCMDoCFaxKSgRKARW0OKGolE6NjIwcLuIcAODRth1P66uah1h1JyHXyQDA8zwsFgtSqVTBAQC2DM/n85r5giDDPnkHzzcPyZIGtFgs6Ovrg91uRzweRyAQwNjYGGRZ3vCamI/ipOF0e9CpJyxXDphMJvT398NutwMA3G43uru70dHRsaEYADCXZu9ks7vEg5Iup3md9vZ2WK1WzbDf78fg4KCmx7JEd3Wu+ZWS4C/JL19MxO5fmbMs/lNs6vV6tLS0aIjj4+PIZDIwGAyaPgUhn4fvaspdeehdAGBf79SZfr78uKt2+3fK7MXaPYQQuFwutLa2lkiKoiAWi8Hn80EQBEiShIWFhQKoKhllH5+ulhin1fjJbuapgxeafa7vVxPZ/aWVbTabxsX09DQ8Hg9GR0fhdrvR29sLjlt/TxVopGP6gcgzzMIVEmRMdnPnA+7UZQt7s3r9XliN4Pz8PKqrqyGKIgRBgMFgQE1NzfrKQMPMVtTWfSN1hj6oZ4CvlGpn08sHhN9+LQosLS1pBGVZRjQaRSgUQjQaBaUUuVyuhDtTgvnAobPWv6dXzqz/Km+pA6e93xqNxncAIJFIQJbl0lqEECSTSYTDYYTDYTgcDoiiWHJYl9lSn11e+/rZlxr5iuEgiiJ6enowMTEBSinMZjOy2WwJT6fTKM8AVcWWkwNt7x9zUZGrJAgAkUgEkUgEgiDA4/EgGAxWnKOAsqYox3Qm+hegDQc3z/MxhvmfRPtPybI8Pzw8fGvxu+SQUjqTz+cbu41Vj+zhjF49Q9g/TRkptfOP1eTton5y0Zd/YkecbLtpOfRGf+7HMsFs+QE35uGRo3sVlezzHz/Xdq2Kd09Ods3xcoPuNe8Xq/W23O9GR/DEZo43DFj/BeI6F/gwl+Hue/7U3SdH99py9xgdP5woJOLG9S+2F3zuid6QbAAAAABJRU5ErkJggg=="></a>
    <a href="https://modrinth.com/plugin/impulse-server-manager"><img src="https://img.shields.io/badge/dynamic/json?labelColor=black&color=1bd96a&label=Modrinth&query=title&url=https://api.modrinth.com/v2/project/UDyKMCWP&style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAAIGNIUk0AAHomAACAhAAA+gAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAJPUExURQAAABvZahWnUha1WAYzGQlHIxvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZahvZav///9ScwmYAAADDdFJOUwAAAAAAAA8zW3uOYwIBK3rB6Pn+ml18KiGL5HEDquOIH07R/UzKz2zu+uLHIibtafWkVCMNBRqg7/RQuT8EQbvT+5ETDBSU/NAgCSdZlcQKii7mtxJY5fF/7D1SRkB+EcWh4UilOOtPMdTCR1PqN969vmGDCw7G4DSSsAcGHrSPr3bds5CEwDKoXumcZdwcG4KmjayX32A79pjOqRjIFoXynVYVgPi6qxDnL78p2obVJFquNbGZCPdyvHPZ1yhuh8s+iRzcsrEAAAABYktHRMQUDBvhAAAAB3RJTUUH5wQXDwgZWDUtiQAAAqRJREFUOMttU/k7lFEU/k4L4hsxtNAnhRgiJZOiSfbI2oJpmRFCi2kV0aaNVLTIEpVISmnf97r/WO+534yZnqfzw73vOe+559xz7rmKIoVYvLx95vj6qUSqVBW3sGrwnxsQaAwSwcDz5i9g0wxPfmFIqJCyiEgLE4vDl7iD8PGlETotIqOQahlAdJTLA5sWY5JsbNzysHiihEBWViRqRDOlgyFmJVtMq5JWJ5vhvyZlLevrEmUILKl8PihtvYWcYt6QLmNwFjjEb4SSkZnlZLO5yJzcPBg35bODwZfPZxYwWbC5sKjYzwBUkssxSg1wKNsCtHUb89vTy3GwopBxDmcJ9YdDJYB1B9t2WmUtcbtkqt18Mxsp9irsezS2VEu+uka/mqUWSrFd2VsnhLEetoZ9TFv3HwCexdUdjBWi0aH4YTuEvtoPM3/EO1nnuTpEjD2qHIM54DhR0wmA5hZySzIntyknsbZyi8IB0tp07tRpBDKfgeWsdDjHDlFGRIjnl3O0t573gcMFUBeVS1gvlxB1dOZxv0pIu9IF01UDZRdjr1SuZQhxvbvnRrmsMePmrdsrGPRaKP8OLtmn9KM7A4N3xb8yNExUb5RlZt2btpZbXajzPoYkBaDKrtADp3HkYdloo56ndgx37UcHxSP0tIWBGH9cg1nx6TJFTNhS7eCfcA1PJ+GgDbLDs2GuX3V05Ohj0xYSBGumyq/yfApw4EWTRxPJqxQvIF6+ks9Gr0d4nN+8Nbhoi+NdJEwVSc6hVNvHOUvg+5qeDxoP3GQz6x8/qXLuZ6Op7SPy+gNTnydSELabk1Z8Mbs/hlr4dbobfUgxJMS3UdXza9H33jqnA3/OH41FCZ7/l7HW8vOXiWc9GvPf06D953/T2O/EP8HBNtcH0Zm/lqFNUgTAex4AAAAldEVYdGRhdGU6Y3JlYXRlADIwMjMtMDQtMjNUMTU6MDg6MjQrMDA6MDAE5dOaAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDIzLTA0LTIzVDE1OjA4OjI0KzAwOjAwdbhrJgAAACh0RVh0ZGF0ZTp0aW1lc3RhbXAAMjAyMy0wNC0yM1QxNTowODoyNSswMDowMITaQU0AAAAASUVORK5CYII="></a>
</p>

## Installation

In short, download our latest release from one of our sources and place it in your Velocity plugins folder. For more
detailed instructions see our [documentation](https://arson.club/impulse/getting_started/installation.html) guide.

Sources:

- [Modrinth](https://modrinth.com/plugin/impulse-server-manager)
- [Hangar](https://hangar.papermc.io/ArsonClub/Impulse)
- [GitHub Releases](https://github.com/Arson-Club/Impulse/releases)

## Quick Start

> [!TIP]
> Looking for a more in-depth guide? See our [Getting Started](https://arson.club/impulse/getting-started.html)
> documentation.
> The following configuration should get you started with a simple lobby server.

### Step 1: Configure Velocity

Simply add the server to your velocity config as normal.

```toml
player-info-forwarding = "modern"

[servers]
lobby = "127.0.0.1:25566"

try = ["lobby"]

```

### Step 2: Configure Impulse

Configure impulse so it know how to manage your server

```yaml
instanceName: Bones
servers:
  - name: lobby
    inactiveTimeout: 300
    type: docker
    docker:
      image: itzg/minecraft-server
      portBindings:
        - "25566:25565"
      env:
        ONLINE_MODE: "FALSE"
        TYPE: "FABRIC"
        EULA: "TRUE"
        MODRINTH_PROJECTS: "fabricproxy-lite"
        DIFFICULTY: "PEACEFUL"
        ALLOW_NETHER: "FALSE"
        MODE: "adventure"
      volumes:
        "/srv/lobby:/data"
```

### Step 3: Configure the MC Server

Add some config to allow for modern forwarding

```toml
# create the file /srv/lobby/config/FabricProxy-Lite.toml
hackOnlineMode = true
hackEarlySend = false
hackMessageChain = true
disconnectMessage = "This server requires you to connect through the proxy."
secret = "<YOUR SECRET FROM forwarding.secret FILE>"
```

### Step 4: Connect

Simply start your velocity proxy and connect to it from your Minecraft client. If you run into issues check
our [documentation](https://arson.club/impulse/getting-started.html) or open an issue!

## Key Features

Impulse has many features to make managing your servers easy! Some highlights include:

* Dynamic server creation and destruction - only run servers when your players are connected
* Automatic hot reload of configuration - update how impulse runs a server automatically, without a reload command!
* Unmanaged server support - plays nice with any static servers you have configured in Velocity
* Custom events - broadcasts custom events that allow you to extend Impulse's functionality
* Third party Broker support - Implement your own Broker or use someone else's to manage your servers
* FOSS - Impulse is commited to being free and open source always

## Documentation

For more detailed information on how to use Impulse, see our [documentation](https://arson.club/impulse/).
For API documentation, see out [KDocs](https://arson.club/impulse/kdocs/).

### Configuration Hot Reload and Reconciliation

Impulse supports hot reloading of its configuration. This even extends to the server configurations!

When a config change is detected a custom `ConfigReloadEvent` is fired that you can listen for in your own plugins. This
allows you to inject your own logic into the reload process from your plugins. You can even create "virtual" servers
this
way that do not exist in the config file.

If a server's configuration is changed, Impulse will attempt to reconcile the running server to match the new
configuration.
The exact behaviour of this is broker specific, but in general it will involve stopping the server, applying the new
configuration,
and restarting automatically. You can control this behavior with the `forceServerReconciliation` and
`reconciliationGracePeriod`
If `forceServerReconciliation` is set to `true` Impulse will immediately trigger a server reconciliation. If set to
`false`
Impulse will only reconcile the server when it naturally restarts, such as after all players leave and the
`inactiveTimeout`
is reached. If the reconciliation requires a server restart, the `reconciliationGracePeriod` is the amount of time in
seconds the Impulse will give players to finish up before the server is stopped. During this time a configurable message
will be displayed to all connected clients.

### Docker Broker

This broker connects directly to a Docker daemon. It handles most of the complex start/stop logic internally. Docker is
relatively easy to set up, while still allowing advanced deployments and architectures. This is the "default" broker.

During reconciliation, docker will not automatically restart the server if resources are removed, such as volumes or
ports.
These should be removed on the next restart.

The docker broker is capable of connecting to a remote docker daemon. This is useful for advanced setups where you want
to run the servers on a different machine. You can find instructions on how to set up a remote docker daemon
[here](https://docs.docker.com/engine/daemon/remote-access/).

## Getting Help

If you require help with Impulse, feel free to open an issue! I will do my best to respond.

## Contributing

All contributions are welcome! For more specific instructions see our [contributing]() documentation.
For specifics on adding creating your own broker to integrate with another server platform see [our guide]().

If you have a feature you would like to see, or a bug you would like to fix, feel free to
open a pull request. If you think it is going to be a large change, feel free to open an issue or a discussion first for
feedback.