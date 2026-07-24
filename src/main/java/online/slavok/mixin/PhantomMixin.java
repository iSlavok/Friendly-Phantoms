package online.slavok.mixin;

// Minecraft 26 removed the type-based LivingEntity#canTarget(EntityType) check and
// drives phantom targeting purely through PhantomAttackPlayerTargetGoal, so 26+
// cancels that goal's canUse() instead. Older (Yarn) versions cancel canTarget.
//? if <1.22 {
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if <1.22 {
@Mixin(PhantomEntity.class)
//?} else {
/*@Mixin(targets = "net/minecraft/world/entity/monster/Phantom$PhantomAttackPlayerTargetGoal")*/
//?}
public class PhantomMixin {
	//? if <1.22 {
	@Inject(method = "canTarget(Lnet/minecraft/entity/EntityType;)Z", at = @At("HEAD"), cancellable = true)
	private void friendlyPhantoms$disableTargeting(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
	//?} else {
	/*@Inject(method = "canUse()Z", at = @At("HEAD"), cancellable = true)
	private void friendlyPhantoms$disableTargeting(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}*/
	//?}
}
