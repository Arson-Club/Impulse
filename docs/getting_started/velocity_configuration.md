# Velocity Configuration

Velocity's configuration is stored in the `velocity.toml` file. For more information on all the available options, see
[Velocity's configuration documentation](https://docs.papermc.io/velocity/configuration).

For this guide we will only need to touch a few options.

## Servers

Due to some limitations in the Velocity API, you need to define any server you would like to directly connect to through
either the `try` or `forced-hoses` blocks in the `velocity.toml` file. We are going to add the connection information
for our SMP server since we want all players to connect to it by default. We will later configure Impulse to adopt the
server reference that velocity creates here.

```toml
[servers]
smp = "127.0.0.1:25566"
```

> [!IMPORTANT]
> Make a note of the server "name" used here. We will need it later.

## Try

The easiest way to get all our players to connect to our SMP by default is to set it as the first (and only) option in
the `try` block. Impulse is not affected by this or the `forced-hosts` settings.

```toml
try = ["smp"]
```

## Player Info Forwarding

Since we are running Velocity in online mode, we can set up player info forwarding to our SMP server so that people get
their skins and correct usernames. Simply change the following option:

```toml
player-info-forwarding = "modern"
```

That is it for the Velocity configuration! Save the file and either reload it with `/velocity reload` or restart the
proxy.