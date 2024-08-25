package online.slavok.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhantomEntity.class)
public class PhantomMixin {
	@Inject(at = @At("HEAD"), method = "canTarget", cancellable = true)
	private void canTarget(EntityType<?> type, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}