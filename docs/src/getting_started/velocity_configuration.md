# Velocity Configuration
Velocity's configuration is stored in the `velocity.toml` file. For more information on all the available options, see
[Velocity's configuration documentation](https://docs.papermc.io/velocity/configuration).

For this guide we will only need to touch a few options.

## Servers
Due to some current limitations, you will still need to define your servers in the `velocity.toml` file. This can be done
as normal. For our simple SMP server, we can use the following configuration:
```toml
[servers]
smp = "127.0.0.1:25566"
```
> [!IMPORTANT]
> Make a note of the server "name" used here. We will need it later.

## Try
For our use case, we want to set up our smp server as the default server for Velocity.
```toml
try = ["smp"]
```

## Player Info Forwarding
Since we are running Velocity in online mode, we can set up player info forwarding to our SMP server so that people get
their skins and correct usernames. Simply change the following option:
```toml
player-info-forwarding = "modern"
```

That is it for the Velocity configuration! Save the file and either reload it with `/velocity reload` or restart the proxy.

