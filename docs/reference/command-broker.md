# Command Broker

The command broker allows you to execute arbitrary commands on the host machine in order to start a server. Impulse will
then manage the associated process, assuming it is a minecraft server. This should be used with caution, or to integrate
new server types for testing. For production you should use or [create](../contributing/creating-a-broker.md) a broker.

> [!WARNING]
> This broker will execute commands with the same permissions and user as the Velocity proxy. You should ensure to limit
> the scope of this user as much as possible. **Do not run your proxy as root!**

## Configuration

Command broker specific configuration values. These should be nested under the `cmd` key in the server configuration.

| Key                | Type           | Description                                                          | Default |
|--------------------|----------------|----------------------------------------------------------------------|---------|
| `workingDirectory` | `string`       | Working directory to set for the spawed subprocess                   | `none`  |
| `command`          | `list[string]` | List of command and flags to execute to start a server               | `none`  |
| `address`          | `string`       | Optional address to use for the server if using dynamic registration |         |