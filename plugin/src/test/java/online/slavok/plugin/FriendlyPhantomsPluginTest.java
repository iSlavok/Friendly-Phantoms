package online.slavok.plugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

/** Smoke test: the plugin loads, enables, and registers its listener on a mock server. */
class FriendlyPhantomsPluginTest {

    private ServerMock server;
    private FriendlyPhantomsPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(FriendlyPhantomsPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void pluginEnablesAndRegistersAListener() {
        assertTrue(plugin.isEnabled(), "plugin must enable cleanly");
        assertFalse(
                org.bukkit.event.HandlerList.getRegisteredListeners(plugin).isEmpty(),
                "a listener must be registered");
    }

    @Test
    void pluginYmlDeclaresFoliaSupportAndApiVersion() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("plugin.yml")) {
            assertNotNull(in, "plugin.yml must be on the classpath");
            String yml = new String(in.readAllBytes());
            assertTrue(yml.contains("folia-supported: true"), "must declare Folia support");
            assertTrue(yml.contains("api-version:"), "must declare api-version");
        }
    }
}
