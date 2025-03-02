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

Currently Docker is the only broker implementation available. Any computer you would like Impulse to run servers on via
this
broker must have docker installed. You can follow the
[official docker installation guide](https://docs.docker.com/get-docker/).

In this example, are running our backend servers on the same machine as our proxy.

> [!TIP]
> For more information on the docker broker itself, see
> the [docker broker documentation](../reference/docker-broker.md).
> You can also find more complex examples in our [Examples]() section.

### JAR

The JAR broker simply requires the Java Runtime to be installed. Since it can only manage servers on the same machine as
Velocity, this should already be installed. You will also need to download the server JAR file you plan to run. In this
example, we will be using the [Fabric server](https://fabricmc.net/use/server/).

> [!TIP]
> For more information on the JAR broker itself, see
> the [JAR broker documentation](../reference/jar-broker.md).