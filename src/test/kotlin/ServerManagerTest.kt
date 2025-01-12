import club.arson.ogsoVelocity.OgsoVelocity
import club.arson.ogsoVelocity.ServiceRegistry
import club.arson.ogsoVelocity.config.ConfigManager
import club.arson.ogsoVelocity.server.ServerManager
import club.arson.ogsoVelocity.server.broker.ServerBroker
import com.velocitypowered.api.proxy.ProxyServer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger

class ServerManagerTest {
    @MockK
    private lateinit var configManager: ConfigManager

    @RelaxedMockK
    private lateinit var proxyServer: ProxyServer

    @MockK
    private lateinit var plugin: OgsoVelocity

    @MockK
    private lateinit var broker: ServerBroker

    @MockK
    private lateinit var logger: Logger

    private lateinit var serverManager: ServerManager

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { configManager.reconcileInterval } returns 10
        ServiceRegistry.instance.configManager = configManager
        serverManager = ServerManager(proxyServer, plugin, broker, logger)
    }

    @Test
    fun testIsRunning() {

    }

}