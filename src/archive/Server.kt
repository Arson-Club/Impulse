package club.arson.ogsoVelocity.server

import club.arson.ogsoVelocity.config.ServerConfig
import com.velocitypowered.api.proxy.server.RegisteredServer

data class Server(
    val server: RegisteredServer,
    var status: ServerStatus,
    var config: ServerConfig
)
