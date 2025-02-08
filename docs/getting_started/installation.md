# Installation

## Download

The Impulse jar file can be downloaded from serveral sources:

- [Modrinth](https://modrinth.com/plugin/impulse-server-manager)
- [Hangar](https://hangar.papermc.io/ArsonClub/Impulse)
- [Offical Github Releases](https://github.com/Arson-Club/Impulse/releases)

For people that need it in a package format, we also publish to
the [Github Maven Repository](https://github.com/orgs/Arson-Club/packages?repo_name=Impulse).

## Installation

To install Impulse simply place the jar file in your Velocity server's `plugins` folder and restart Velocity if it is
running. You can verify Impulse is running by checking the console for the following messages:

```
[18:52:15 INFO] [Impulse]: Initializing Impulse
[18:52:15 INFO] [Impulse]: Configuration reloaded
```

## Alternative Versions

> [!NOTE]
> These versions are for people that are looking to try out new features before others. They are not recommended for
> production servers and may be generally unstable.

### Pre-Release

We will publish pre-release builds to our [Github Releases](https://github.com/Arson-Club/Impulse/releases) page
and [Hangar Snapshots](https://hangar.papermc.io/ArsonClub/Impulse/versions?channel=Snapshot&platform=VELOCITY) channel.
These are often release candidates that have not yet been fully tested, or that we are looking for feedback on. They are
the most stable of our alternative versions since in theory any one could be promoted to a full release.

### Nightly

We maintain a nightly build channel. As the name implies this is a nightly build of `main` bugs and all. You can find
obtain these from our [Github Actions]() page. These are the least stable of our versions.

## Advanced Installation

For more advanced installation options, such as custom brokers or using our "minimal" jar, see
the [Advanced Installation](../reference/advanced-installation.md) guide.