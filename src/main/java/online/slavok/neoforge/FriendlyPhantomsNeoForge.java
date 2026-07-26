package online.slavok.neoforge;

import net.minecraft.world.entity.monster.Phantom;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import online.slavok.PhantomBehavior;

/**
 * NeoForge entrypoint. Instead of the Fabric mixin, NeoForge cancels the phantom's
 * target change through {@link LivingChangeTargetEvent}: a phantom that is never
 * allowed to acquire a target cannot attack, while spawning and membrane drops are
 * untouched. This is only compiled on NeoForge nodes (see the source-set excludes
 * in build.neoforge.gradle.kts) and uses Mojang mappings.
 */
@Mod("friendly_phantoms")
public class FriendlyPhantomsNeoForge {

    private static final Logger LOGGER = LoggerFactory.getLogger("friendly_phantoms");

    public FriendlyPhantomsNeoForge() {
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Friendly Phantoms loaded — phantoms will no longer attack.");
    }

    @SubscribeEvent
    public void onChangeTarget(LivingChangeTargetEvent event) {
        if (PhantomBehavior.friendly && event.getEntity() instanceof Phantom) {
            event.setCanceled(true);
        }
    }
}
