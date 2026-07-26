package online.slavok.plugin;

import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Pure unit tests for the target-suppression logic. Uses Mockito for the entity
 * interfaces so the test never depends on MockBukkit's per-entity mock coverage.
 * {@link EntityTargetEvent} is a concrete event class, so it is used for real.
 */
class PhantomTargetListenerTest {

    private final PhantomTargetListener listener = new PhantomTargetListener();

    @Test
    void phantomTargetingPlayerIsCancelledAndCleared() {
        Phantom phantom = mock(Phantom.class);
        Player player = mock(Player.class);
        EntityTargetEvent event =
                new EntityTargetEvent(phantom, player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER);

        listener.onTarget(event);

        assertTrue(event.isCancelled(), "phantom→player target must be cancelled");
        assertNull(event.getTarget(), "target must be cleared to null");
    }

    @Test
    void nonPhantomTargetingPlayerIsUntouched() {
        Zombie zombie = mock(Zombie.class);
        Player player = mock(Player.class);
        EntityTargetEvent event =
                new EntityTargetEvent(zombie, player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER);

        listener.onTarget(event);

        assertFalse(event.isCancelled(), "non-phantom must be untouched");
        assertSame(player, event.getTarget());
    }

    @Test
    void phantomTargetingNonPlayerIsUntouched() {
        Phantom phantom = mock(Phantom.class);
        Zombie other = mock(Zombie.class);
        EntityTargetEvent event =
                new EntityTargetEvent(phantom, other, EntityTargetEvent.TargetReason.CLOSEST_ENTITY);

        listener.onTarget(event);

        assertFalse(event.isCancelled(), "phantom targeting a non-player must be untouched");
        assertSame(other, event.getTarget());
    }
}
