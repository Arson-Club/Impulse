# Impulse Configuration

Impulse maintains its own configuration file named `config.yaml`. It can be found in the `plugins/impulse` directory.
If it does not exist you can create it. For our simple SMP we can keep most of the defaults. For now lets set our
`instanceName`.

`plugins/impulse/config.yaml`:

```yaml
instanceName: MyCoolSMP
```

The instance name is used internally by Impulse to identify your Velocity server. This is necessary for some brokers
that may be managing servers for multiple Velocity instances. It should be unique per Velocity server.

## Selecting a Broker

In Velocity a "broker" is an interface that Impulse uses to actually interact with different platforms that can host
minecraft servers. For the next step select which broker you would like to use, and follow the instructions for that
broker. You can find a basic description of each broker below with pros and cons. For more in depth information see
the [brokers](../reference/brokers.md) documentation.

### Docker

The Docker broker is a good choice for most users. It offers a good balance of flexibility and ease of setup. It also
allows for running servers on multiple machines.

[Continue to Docker Broker Setup](docker_broker.md)

### JAR

The JAR broker is the simplest broker. It primarily designed for a single server setup and development. It only supports
running servers on the same machine as Velocity.

[Continue to JAR Broker Setup](jar_broker.md)

### Kubernetes

> [!WARNING]
> This broker in not yet available. It is planned for a future release.

The Kubernetes broker is for advanced setups and those already familiar with Kubernetes. It is the most powerful broker,
but also significantly more complex to setup.

[NOT YET AVAILABLE]()
