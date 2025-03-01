# JAR

The JAR broker is simple to configure. For our SMP server the following will provide a minimal working configuration.

### `plugins/impulse/config.yaml`

```yaml
instanceName: MyCoolSMP
servers:
  - name: smp
    type: jar
    lifecycleSettings:
      timeouts:
        inactiveGracePeriod: 300
    jar:
      workingDirectory: /srv/smp
      jarFile: fabric.jar
      javaFlags:
        - -Xms4G
        - -Xmx4G
      flags:
        - --nogui
```

Let's break that down a bit!

## `instanceName`

Here we give a unique identifier to our Velocity server. This is not used by the JAR broker, but others may use it to
identify different Velocity instances that they are managing servers for.

## `servers` section

This is where we can define a list of servers that we want Impulse to manage. In this case, we are defining a single
server named `smp`.

`name` provides a unique identifier for the server.

> [!IMPORTANT]
> The name of the server in the `servers` section must match the name of the server Velocity's `velocity.toml`
> ***exactly***. If it does not you will get unexpected behaviour!

`lifecycleSettings` is where we can set all the config properties related to how a server runs. For our SMP server we
are setting the amount of time the server will wait before shutting down after the last player has left to 5 min.

`type` specifies which broker is managing this server. In this case, we are using the `jar` broker.

### `jar` section

This section contains all the configuration specific to the JAR broker. If you are familiar with running a Minecraft
servers from the command line this may look familiar.

`workingDirectory` specifies the directory that the server will be run from. This should be the directory that contains
the server jar.

`jarFile` specifies the server jar file to run.

`javaFlags` allows us to specify flags to pass to the JVM when starting the server. In this case, we are setting the max
and min heap size to 4G.

`flags` allows us to specify flags to pass to the server jar. In this case, we are telling the server to run in nogui
mode.

This configuration will cause the server to be run as if you had run the following command from the `/srv/smp`
directory:

```shell
java -Xms4G -Xmx4G -jar fabric.jar --nogui
```

### Configuring the Server

> [!TIP]
> If you have not started the server before, run it once manually so it can generates its associated files.

We will need to handle some configuration of our server if we want it to be able to connect to our Velocity proxy.

#### `server.properties`

Because Velocity is running on port `25565` we will need to change the server port in the `server.properties` file to
`25566`.

```properties
server-port=25566
```

#### `eula.txt`

You will also need to agree to the Minecraft EULA. Open the `eula.txt` file and change `eula=false` to `eula=true`.

```properties
eula=true
```

#### Identity Forwarding

Since Velocity is set up with "modern" forwarding and in online mode, we need a bit of configuration on our server
before players will be able to connect. Luckily this is very simple! We just need to add a file to the `/srv/smp` and
install a mod.

First download the [FabricProxy-Lite](https://modrinth.com/mod/fabricproxy-lite) mod and place it in your mods folder.
Once that is installed you can add the following file to the `/srv/smp/config` directory.

`/srv/smp/config/FabricProxy-Lite.toml`

```toml
hackOnlineMode = true
hackEarlySend = false
hackMessageChain = true
disconnectMessage = "This server requires you to connect with Velocity."
secret = "<YOUR SECRET FROM forwarding.secret HERE>"
```

You can find the `forwarding.secret` file next to your `velocity.toml`.