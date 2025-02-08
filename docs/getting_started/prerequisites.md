# Prerequisites

## Velocity

> [!WARNING]
> Impulse does not condone and will not provide support for proxies running in offline mode.

This guide assumes you already have a working velocity server. If you are setting up from scratch, you can follow
[Velocity's getting started guide](https://docs.papermc.io/velocity/getting-started). Once configured, you can proceed
with setting up Impulse. Additionally, this guide will assume you
have [configured player forwarding](https://docs.papermc.io/velocity/player-information-forwarding) and are running your
proxy in online mode.

## Brokers

Later in this guide we will be configuring our backed servers using one of Impulse's broker implementations. Depending
on which broker you choose, you may need to install additional software on your backend servers.

### Docker

Currently Docker is the only broker implementation available. Any machine that you would like Impulse to manage backend
servers must have docker installed. You can follow
the [official docker installation guide](https://docs.docker.com/get-docker/) to install docker on your machine.

In this example, we will running our backend servers on the same machine as our proxy. If you run your proxy on a
separate machine, docker is not required for that computer.

> [!TIP]
> For more information on the docker broker itself, see
> the [docker broker documentation](../reference/docker-broker.md).
> You can also find more complex examples in our [Examples]() section.
