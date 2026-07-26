package online.slavok.plugin;

import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Stops phantoms attacking players while leaving spawning and membrane drops
 * untouched — the server-plugin equivalent of the mod's {@code canTarget} mixin.
 *
 * <p>Only target acquisition is intercepted: cancelling blocks the player from
 * being taken as a new target, and {@code setTarget(null)} clears any target
 * assigned this tick (cancelling alone would let the phantom finish an in-flight
 * attack). Phantoms still spawn and can be killed by the player, so membranes
 * remain farmable.
 */
public final class PhantomTargetListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Phantom && event.getTarget() instanceof Player) {
            event.setTarget(null);
            event.setCancelled(true);
        }
    }
}
