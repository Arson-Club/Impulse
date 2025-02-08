# Docker

To configure our SMP server to be managed by Docker, we simply need to add a few lines to our `config.yaml` file.

### `plugins/impulse/config.yaml`

```yaml
instanceName: MyCoolSMP
servers:
  - name: smp
    type: docker
    lifecycleSettings:
      timeouts:
        inactiveGracePeriod: 300
    docker:
      image: "itzg/minecraft-server:latest"
      portBindings:
        - "25566:25565"
      env:
        ONLINE_MODE: "FALSE"
        TYPE: "FABRIC"
        EULA: "TRUE"
        MODRINTH_PROJECTS: "fabricproxy-lite"
      volumes:
        - "/srv/smp:/data"
```

Let's break that down a bit!

## `servers` section

This is where we can define a list of servers that we want Impulse to manage. In this case, we are defining a single
server named `smp`.

`name` provides a unique identifier for the server.
> [!IMPORTANT]
> The name of the server in the `servers` section must match the name of the server Velocity's `velocity.toml`
***exactly***. If it does not you will get unexpected behaviour!

`lifecycleSettings` is where we can set all the config properties related to how a server runs. For our SMP server we
are setting the amount of time the server will wait before shutting down after the last player has left to 5 min.

`type` specifies which broker is managing this server. In this case, we are using the `docker` broker.

### `docker` section

This section contains all the configuration specific to the Docker broker. If you are familiar with Docker Compose or
Kubernetes many of these options may look familiar. If not, never fear! We will explain them all.

`image` specifies the Docker image that we want to use for this server. In this case, we are using the
`itzg/minecraft-server` image. This image is a very popular image for running Minecraft servers in Docker containers and
is the default. Documentation can be found [here](https://docker-minecraft-server.readthedocs.io/en/latest/). Impulse
will work with any image, so feel free to change this as needed.

`portBindings` allows us to define how we want to map ports from the host to the container. In this case, we are mapping
all traffic on the *host* port `25566` to the *container* port `25565`.

`env` allows us to define environment variables that will be passed to the container. The `itzg/minecraft-server` image
has [many environment variables](https://docker-minecraft-server.readthedocs.io/en/latest/variables/) that can be used
to configure your MC server. In this case, we are setting up a Fabric server with the `fabricproxy-lite` mod. It is also
set to offline mode as required by Velocity.

`volumes` allows us to define which directories on the host we want to mount into the container. In this case, we are
mounting the `/srv/smp` directory on the host to the `/data` directory in the container. This is where the Minecraft
server data will be saved. The source directory can be any directory on the host, but it must be mounted to `/data` for
this image.
> [!IMPORTANT]
> If you do not set up a volume mount for the `/data` directory, ***ALL YOUR SERVER DATA WILL BE LOST*** on restarts.

## Configuring Identity Forwarding

Since Velocity is set up with "modern" forwarding and in online mode, we need a bit of configuration on our server
before players will be able to connect. Luckily this is very simple! We just need to add a file to the `/srv/smp`
directory that we mounted into the container.

### `/srv/smp/config/FabricProxy-Lite.toml`

```toml
hackOnlineMode = true
hackEarlySend = false
hackMessageChain = true
disconnectMessage = "This server requires you to connect with Velocity."
secret = "<YOUR SECRET FROM forwarding.secret HERE>"
```

You can find the `forwarding.secret` file next to your `velocity.toml`.

## Pre-pulling the Docker Image

> [!TIP]
> This is not strictly required, but highly recommended. It will speed up the server start time and connection times.
> To pre-pull the docker image, simply run the following command on your backend server:

```bash
docker pull itzg/minecraft-server:latest
```
