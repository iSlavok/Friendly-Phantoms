package online.slavok.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhantomEntity.class)
public class PhantomMixin {
	@Inject(method = "canTarget(Lnet/minecraft/entity/EntityType;)Z", at = @At("HEAD"), cancellable = true)
	private void friendlyPhantoms$disableTargeting(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}
