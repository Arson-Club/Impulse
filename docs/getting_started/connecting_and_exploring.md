# Connecting and Exploring

Velocity is set up to hot reload its configuration when it changes. This means you should not have to restart Velocity.
You can verify that your configuration is loaded by looking for the following log message in the Velocity log:

```
[18:52:15 INFO] [Impulse]: Configuration reloaded
[18:52:16 INFO] [Impulse]: ServerManager: server smp reconciled
```

## Connecting to the server

If you see this then you should be all set to connect to velocity as normal! When connecting you should notice that you
stay on the join screen for longer than normal. This is because Impulse is starting the server in the background. Once
the server is started you will be connected to it automatically.

|            ![join screen](../assets/images/joining.png)            | 
|:------------------------------------------------------------------:| 
| *The join screen will look like this while the server is starting* |

If the server takes longer to start than your configured timeout you will be disconnected with an error message. The
server will still attempt to start in the background. Try and reconnect after a few seconds. If the error persists see
the [debugging](#debugging) section.

|      ![error screen](../assets/images/error.png)       | 
|:------------------------------------------------------:| 
| *Error screen if the server start exceeds the timeout* |

All connections after the first "cold" start should be much faster as the server is already running. Only the first
player to connect after the server has stopped will experience the delay.

## Trying out the commands

> [!IMPORTANT]
> These commands will work from both in game and the server console. If you want to follow along in game, you will need
> to install a command management plugin like [LuckPerms](https://luckperms.net/) into velocity and grant yourself the
> `impulse.server.*` permission.
> ***You must use a command manager to access these in game. Being OP is not enough.***

Now that we are on the server lets take a look at some of the commands that Impulse provides. The full command list and
documentation can be found [here](../reference/commands.md).

### `impulse status`

If you run the status command it will give you the current status of all servers that Impulse is managing. You can also
request the status of a specific server by passing the server name as an argument.

### `impulse start`

This command will start a server that is currently stopped. If the server is already running it will do nothing.

### `impulse stop`

This command will stop a server that is currently running. This includes the server that you are currently on.

## Leaving the server

When you are done exploring the server you can disconnect. If you are the only player on the server Impulse will stop
the server after the configured timeout. Be default this is 5 minutes. You can see this in the logs when a server is
stopped:

```
[05:19:04 INFO] [impulse]: Server smp has no players, stopping
```

## Debugging

If you are using the docker broker you can check the container status by running the following command:

```shell
docker ps -a
```

you should see a container with the name `smp` and the status `Up`. If the status is `Exited` then the server has
crashed for some reason. If the docker status is Up, but you are still having issues connecting to the server it may
still be starting. For either case you can check the logs of the container by running:

```shell
docker logs smp
```

Look for any errors in the logs, or wait for the server to finish starting. If you are still having issues feel free to
open an issue on the [GitHub repository](https://github.com/Arson-Club/Impulse/issues)

At this point you should be able to continue to set up your server as you see fit.