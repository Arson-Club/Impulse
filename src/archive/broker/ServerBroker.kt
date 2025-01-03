package club.arson.ogsoVelocity.server.broker

import club.arson.ogsoVelocity.server.Server

interface ServerBroker {
    fun startServer(server: Server): Result<Unit>
}