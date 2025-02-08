# Advanced Installation

> [!TIP]
> For a basic installation and setup guide, see the [Getting Started](../getting_started/index.md) page.

The default distribution of Impulse includes several default brokers. This is useful for getting started quickly, but
not optimal for production environments. This section will outline how to use the "lite" distribution of Impulse to set
up your server with just the brokers you need. Some benefits of this approach include:

- Smaller jar size: The default distribution includes several brokers, which can be quite large. The "lite" distribution
  includes only the brokers you need.
- Faster startup time: No need to initialize brokers you don't need.
- Smaller attack surface: Fewer brokers means fewer potential vulnerabilities.

## Downloading the "Lite" Distribution

You can find the "lite" distribution of Impulse on the [releases page](https://github.com/Arson-Club/Impulse/releases)
or under the "Lite" channel on [Hangar](https://hangar.papermc.io/ArsonClub/Impulse). Download the jar file and place it
in the plugins folder for Velocity.

## Adding a Broker

To add a broker to your server, you will need to download the broker jar file and place it in the `plugins/impulse`
folder in Velocity. The broker will be loaded automatically when the server starts. You can find first party brokers on
the [releases page](https://github.com/Arson-Club/Impulse/releases).