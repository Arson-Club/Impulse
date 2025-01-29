# Brokers

Impulse offloads the low level server management to a "broker". These brokers deal with implementing the high level
server abstractions from Impulse into a specific platform. This allows Impulse to be more flexible and support a wide
range of platforms.

We provide several first party brokers. You can find more information on each broker below.

- [Docker](docker-broker.md)
- [JAR]()
- [Kubernetes]()

Additionally, you can create your own broker. For more information see our guide
on [creating a broker](../contributing/creating-a-broker.md). Here is a list of some notable third party brokers:
> [!WARNING]
> These brokers are not tested or maintained by the Impulse team. Make sure to verify them and report any issues to the
> respective authors.

- None yet. Feel free to submit a PR to add your broker here!