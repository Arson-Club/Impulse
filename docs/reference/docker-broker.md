# Docker Broker

The docker broker connects directly to a Docker daemon. It is a good choice for small to medium-sized deployments since
it is relatively easy to set up while still having a good amount of advanced options for configuration. It is included
in the default Impulse distribution.

The docker broker is capable of connecting to a remote docker daemon. This is useful for advanced setups where you want
to run the servers on a different machine. You can find instructions on how to set up a remote docker daemon
[here](https://docs.docker.com/engine/daemon/remote-access/).
> [!WARNING]
> Connecting over TLS is not fully supported/tested yet. Only do this over a network you trust.

## Configuration

Docker broker specific configuration values. These should be nested under the `docker` key in the server configuration.

| Key                 | Type           | Description                                                                                                                                                                                      | Default                       |
|---------------------|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------|
| `image`             | `string`       | Docker image to use for the server                                                                                                                                                               | `itzg/minecraft-server`       |
| `address`           | `string`       | Used to specify the connection address when configured as a dynamic server. Format is `host:port`                                                                                                | `null`                        |
| `imagePullPolicy`   | `enum`         | One of `Always`, `Never`, `IfNotPresent`. Controls under what circumstance we should attempt to pull the configured image automatically. See [Image Pull Policy](#Image-Pull-Policy) for details | `IfNotPresent`                |
| `autoStartOnCreate` | `boolean`      | Controls autostart behaviour for the container. If set to true we will start the container at creation time, not when a user connects.                                                           | `false`                       |
| `portBindings`      | `list<string>` | List of port bindings to use for the server. Each entry should be a string in the format `hostPort:containerPort`                                                                                | `["25565:25565"]`             |
| `hostPath`          | `string`       | URI to the docker daemon location. This can either be a local socket, or a remote host.                                                                                                          | `unix:///var/run/docker.sock` |
| `tlsConfig`         | `object`       | TLS related configuration options if connecting to the docker daemon over TCP                                                                                                                    |                               |
| `volumes`           | `list<string>` | List of host directories to mount into the container. These should be strings in the format `/host/path:/container/path`                                                                         | `{}`                          |
| `env`               | `Map`          | Map of environment variables to pass to the container. The key is the variable name, and the value is the variable value.                                                                        | `{"ONLINE_MODE": "false"}`    |

### TLS Configuration

| Key                 | Type      | Description                                                                            | Default |
|---------------------|-----------|----------------------------------------------------------------------------------------|---------|
| `enabled`           | `boolean` | Should we enable TLS for this docker connection                                        | `false` |
| `tlsVerify`         | `boolean` | Should we verify the host certificate for this connection                              | `true`  |
| `clientKeyPassword` | `string`  | Password for the client key if it differs from the `keystorePassowrd`                  | `null`  |
| `keystorePath`      | `string`  | Java PKCS12 formatted keystore that contains your client cert, client key, and CA cert | `null`  |
| `keystorePassword`  | `string`  | Password for accessing the configured keystore                                         | `null`  |

### Image Pull Policy

The image pull policy controls when we should attempt to pull the configured image. The following policies are
available:

- `Always`: Always attempt to pull the image before starting the container
- `Never`: Never attempt to pull the image before starting the container
- `IfNotPresent`: Only attempt to pull the image if it is not already present on the host

## Reconciliation Behavior

Most changes under the `docker` configuration key will require a server recreation to take effect. The docker broker
will automatically stop, remove, and recreate the server container as needed. Because of this behavior, it is important
to make sure any data you want to keep is stored in a volume. If not it will be lost during a reconciliation event. This
can normally be accomplished by mounting a volume to the `/data` directory in the container.

## Using TLS

Docker uses TLS to secure connections the the daemon over TCP. If you are using a remote docker daemon, it is highly
encouraged to use TLS to secure the connection. The following instuctions will go over how to generate the keystore for
Impulse from your PKI. To set up the certificates and keys in the first place
follow [docker's guide](https://docs.docker.com/engine/security/protect-access/)

### Generating the Keystore

First, If you need to convert your client credentials to PKCS12 format you can use the following command. Replace
`<your_keystore_password>` with a real password.

```shell
openssl pkcs12 -export -in client.crt -inkey client.key \
-out client-keystore.p12 -name client-cert -passout pass:<your_keystore_password>
````

Next generate the keystore by importing the client certificate and key.

```shell
keytool -importkeystore -srckeystore docker-keystore.p12 -srcstoretype PKCS12 \
-srcstorepass <your_keystore_password> -destkeystore docker-keystore.p12 -deststoretype PKCS12 \
-deststorepass <your_keystore_password>
```

Lastly, inject the CA certificate into the keystore.

```shell
keytool -importcert -alias ca-cert -file ca.crt \
-keystore docker-keystore.p12 -storepass your_keystore_password -noprompt
````

Your keystore should now be set up and ready to use with Impulse.

## Example Configuration

```yaml
servers:
  - name: "lobby"
    type: "docker"
    docker:
      portBindings:
        - "25566:25565"
      volumes:
        - "/path/to/host/data:/data"
      env:
        ONLINE_MODE: "false"
        EULA: "true"
```