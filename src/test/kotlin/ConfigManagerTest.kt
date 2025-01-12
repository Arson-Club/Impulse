import club.arson.impulse.Impulse
import club.arson.impulse.config.ConfigManager
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.Scheduler
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.slf4j.Logger
import kotlin.io.path.Path

class ConfigManagerTest {
    @MockK
    private lateinit var proxy: ProxyServer

    @MockK
    private lateinit var plugin: Impulse

    @RelaxedMockK
    private lateinit var logger: Logger

    private lateinit var configManager: ConfigManager

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { proxy.scheduler } returns mockk<Scheduler>()
        configManager = ConfigManager(proxy, plugin, Path("./"), logger)
    }
}