package online.slavok.plugin;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bootstrap: registers {@link PhantomTargetListener}. All behaviour lives in the
 * listener so it can be unit-tested without a server instance.
 */
// Not final: MockBukkit subclasses the plugin at load time.
public class FriendlyPhantomsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PhantomTargetListener(), this);
        getLogger().info("Friendly Phantoms loaded — phantoms will no longer attack.");
    }
}
