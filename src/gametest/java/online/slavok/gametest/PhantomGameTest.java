package online.slavok.gametest;

import online.slavok.PhantomBehavior;
// The GameTest API drifts a lot across versions:
//  - 1.21+ uses the reworked Fabric v3 API (bare @GameTest, no interface); earlier
//    versions use the FabricGameTest interface + vanilla @GameTest (templateName
//    since 1.19, structureName on 1.18).
//  - 26+ is unobfuscated (Mojang names) and removed canTarget(EntityType).
//
// Yarn versions do a full A/B on the pure canTarget() predicate: with the mixin a
// phantom cannot target players, and flipping PhantomBehavior.friendly off proves a
// vanilla phantom can - so the mixin is demonstrably what changes the outcome.
//
// 26+ cancels PhantomAttackPlayerTargetGoal#canUse() instead, which is stateful and
// only fires for real players; gametest has none, so a behavioural A/B is not
// possible. Instead the mixin is `required`, so spawning a phantom (which loads and
// mixes the goal class) reaching succeed() proves the mixin applied to the real
// canUse() - a wrong target would crash the server on class load.
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
    //? if >=1.21 {
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
        // Loads and mixes PhantomAttackPlayerTargetGoal; a wrong mixin target would
        // have crashed on load before this point (the mixin is required).
        Phantom phantom = context.spawn(type, BlockPos.ZERO);
        if (!phantom.isAlive()) {
            context.fail("Phantom failed to spawn");
        } else {
            context.succeed();
        }
    }*/
    //?} else {
    public void phantomCannotTargetPlayers(TestContext context) {
        PhantomEntity phantom = context.spawnEntity(EntityType.PHANTOM, BlockPos.ORIGIN);

        // Mixin enabled: the phantom must not be able to target players.
        if (phantom.canTarget(EntityType.PLAYER)) {
            throw new RuntimeException("Mixin failed: phantom can still target players");
        }

        // Control: with the mixin disabled, a vanilla phantom CAN target players.
        PhantomBehavior.friendly = false;
        try {
            if (!phantom.canTarget(EntityType.PLAYER)) {
                throw new RuntimeException("Control failed: vanilla phantom should target players");
            }
        } finally {
            PhantomBehavior.friendly = true;
        }
        context.complete();
    }
    //?}
}
