# Creating a Broker

Creating a custom broker for Impulse is a simple process! This guide will walk you through the steps to create a broker
for your favorite server software.

> [!TIP]
> The [docker broker source](https://github.com/Arson-Club/Impulse/tree/main/docker-broker) is a great example to follow
> when creating your own broker.

## Including the API

The first step is to include the Impulse API in your project. We publish to the GitHub Package Registry, so you just
have to add the following to your `build.gradle` file:

```groovy
dependencies {
    implementation 'club.arson.impulse:impulse-api'
}

repositories {
    maven {
        name = "Impulse"
        url = uri("https://maven.pkg.github.com/Arson-Club/Impulse")
    }
}
```

## The Broker Interface

The core component of a broker is its implementation of the `Broker` interface (`club.arson.impulse.api.server.Broker`).
Here you will implement all the functions required for impulse to interact with the server software. For specifics on
what each function does, refer to the [KDocs](https://arson-club.github.io/Impulse/kdocs/index.html).

## The Broker Configuration

Most brokers will require some sort of configuration to be able to set up and manage servers properly. To do this,
simply create a data class and annotate it with `@BrokerConfig("your-broker-id")`. Impulse will handle loading your
configuration for you. Make sure to include a `@Serializable` annotation on the class as well.

## The Broker Factory

The final step is to implement the `BrokerFactory` interface (`club.arson.impulse.api.server.BrokerFactory`). This is
used by Impulse to create instances of your broker as needed. The most important thing here is the `NAME` field. It will
be used to identify your broker in the configuration file, and to link it to your Configuration class.

> [!WARNING]
> You must provide at least a Factory and Configuration class for Impulse to load your broker. The IDs must match
> ***exactly***.
