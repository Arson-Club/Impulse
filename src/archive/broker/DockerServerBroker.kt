package club.arson.ogsoVelocity.server.broker

import club.arson.ogsoVelocity.server.Server

class DockerServerBroker : ServerBroker {
    override fun startServer(server: Server): Result<Unit> {
        return Result.failure(UnsupportedOperationException("DockerServerBroker not implemented"))
    }
}