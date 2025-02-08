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
the [brokers](brokers.md) documentation.

> [!WARNING]
> Currently only the [Docker](#Docker) broker is publicly available. The rest are still in development.

### Docker

The Docker broker is a good choice for most users. It offers a good balance of flexibility and ease of setup. It also
allows for running servers on multiple machines.

[Continue to Docker Broker Setup](docker_broker.md)

### JAR

The JAR broker is the simplest broker. It primarily designed for a single server setup and development. It is not
recommended for multi-server configurations.

[NOT YET AVAILABLE]()

### Kubernetes

The Kubernetes broker is for advanced setups and those already familiar with Kubernetes. It is the most powerful broker,
but also significantly more complex to setup.

[NOT YET AVAILABLE]()
