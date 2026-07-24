package online.slavok.gametest;

// Fabric reworked the GameTest API in 1.21 (v3: bare @GameTest, no interface).
// Earlier versions use the FabricGameTest interface and vanilla @GameTest
// (templateName since 1.19, structureName on 1.18). Failure is signalled by a
// thrown exception, which every version treats as a failed test - this avoids the
// assertTrue message type churning across versions (String vs Text).
//? if >=1.21 {
import net.fabricmc.fabric.api.gametest.v1.GameTest;
//?} else {
/*import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;*/
//?}
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

//? if >=1.21 {
public class PhantomGameTest {
//?} else {
/*public class PhantomGameTest implements FabricGameTest {*/
//?}
    // Directly exercises PhantomMixin: a phantom must report it cannot target players.
    //? if >=1.21 {
    @GameTest
    //?} elif >=1.19 {
    /*@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)*/
    //?} else {
    /*@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE)*/
    //?}
    public void phantomCannotTargetPlayers(TestContext context) {
        PhantomEntity phantom = context.spawnEntity(EntityType.PHANTOM, BlockPos.ORIGIN);
        if (phantom.canTarget(EntityType.PLAYER)) {
            throw new RuntimeException("Phantom must not be able to target players");
        }
        context.complete();
    }
}
