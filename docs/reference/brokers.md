# Brokers

Impulse offloads the low level server management to a "broker". These brokers deal with implementing the server
abstractions from Impulse into a specific platform. This allows Impulse itself to be smaller. You only need to include
the brokers you will be using. The docker broker is included in the default distribution. The "lite" distribution does
not include any brokers.

## Dynamic Broker Loading

Impulse is capable of dynamically loading additional brokers at startup. To add a broker, place the jar in
Impulse's data directory (normally `plugins/impulse`). Hot reloading is not currently supported. You will need to
restart
Velocity to update add a new broker or update an existing one.

## First Party Brokers

We provide several first party brokers. You can find more information on each broker below.
> [!NOTE]
> Not all brokers are available in the default distribution. You may need to add them to your Impulse installation.

- [Docker](docker-broker.md) **(Included in default distribution)**
- [JAR]()
- [Kubernetes]()

## Third Party Brokers

Additionally, you can create your own broker or source them from others. For more information see our guide
on [creating a broker](../contributing/creating-a-broker.md). Here is a list of some notable third party brokers:
> [!WARNING]
> These brokers are not tested or maintained by the Impulse team. Make sure to verify them and report any issues to the
> respective authors.

- None yet. Feel free to submit a PR to add your broker here!