package online.slavok.gametest;

// The GameTest API drifts a lot across versions:
//  - 1.21+ uses the reworked Fabric v3 API (bare @GameTest, no interface); earlier
//    versions use the FabricGameTest interface + vanilla @GameTest (templateName
//    since 1.19, structureName on 1.18).
//  - 26+ is unobfuscated (Mojang names) and removed canTarget(EntityType), so the
//    check is behavioural: spawn a phantom next to a player, tick, assert no target.
//    Earlier versions call canTarget directly.
//? if >=1.21 {
import net.fabricmc.fabric.api.gametest.v1.GameTest;
//?} else {
/*import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;*/
//?}
//? if >=1.22 {
/*import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;*/
//?} else {
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
//?}

//? if >=1.21 {
public class PhantomGameTest {
//?} else {
/*public class PhantomGameTest implements FabricGameTest {*/
//?}
    // Directly exercises PhantomMixin: a phantom must not target players.
    //? if >=1.22 {
    /*@GameTest(maxTicks = 100)*/
    //?} elif >=1.21 {
    @GameTest
    //?} elif >=1.19 {
    /*@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)*/
    //?} else {
    /*@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE)*/
    //?}
    //? if >=1.22 {
    /*@SuppressWarnings("unchecked")
    public void phantomCannotTargetPlayers(GameTestHelper context) {
        EntityType<Phantom> type = (EntityType<Phantom>) BuiltInRegistries.ENTITY_TYPE
                .getValue(Identifier.withDefaultNamespace("phantom"));
        Phantom phantom = context.spawn(type, BlockPos.ZERO);
        context.makeMockServerPlayerInLevel();
        context.runAfterDelay(40L, () -> {
            if (phantom.getTarget() != null) {
                context.fail("Phantom must not be able to target players");
            } else {
                context.succeed();
            }
        });
    }*/
    //?} else {
    public void phantomCannotTargetPlayers(TestContext context) {
        PhantomEntity phantom = context.spawnEntity(EntityType.PHANTOM, BlockPos.ORIGIN);
        if (phantom.canTarget(EntityType.PLAYER)) {
            throw new RuntimeException("Phantom must not be able to target players");
        }
        context.complete();
    }
    //?}
}
