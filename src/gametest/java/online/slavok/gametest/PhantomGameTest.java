package online.slavok.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PhantomGameTest {
    // Directly exercises PhantomMixin: a phantom must report it cannot target players.
    @GameTest
    public void phantomCannotTargetPlayers(TestContext context) {
        PhantomEntity phantom = context.spawnEntity(EntityType.PHANTOM, BlockPos.ORIGIN);
        context.assertTrue(
            !phantom.canTarget(EntityType.PLAYER),
            Text.literal("Phantom must not be able to target players")
        );
        context.complete();
    }
}
