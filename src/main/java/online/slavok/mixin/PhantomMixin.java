package online.slavok.mixin;

// Minecraft 26+ ships unobfuscated (Mojang names); earlier versions use Yarn.
//? if <1.22 {
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
//?} else {
/*import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;*/
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if <1.22 {
@Mixin(PhantomEntity.class)
//?} else {
/*@Mixin(Phantom.class)*/
//?}
public class PhantomMixin {
	//? if <1.22 {
	@Inject(method = "canTarget(Lnet/minecraft/entity/EntityType;)Z", at = @At("HEAD"), cancellable = true)
	//?} else {
	/*@Inject(method = "canAttackType(Lnet/minecraft/world/entity/EntityType;)Z", at = @At("HEAD"), cancellable = true)*/
	//?}
	private void friendlyPhantoms$disableTargeting(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}
