package net.koyclient.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.koyclient.KoyClient;
import net.koyclient.module.ZoomModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void koyclient$zoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (KoyClient.modules() == null) return;
        ZoomModule zoom = KoyClient.modules().get(ZoomModule.class);
        if (zoom != null && zoom.isEnabled() && zoom.factor() > 1.001f) {
            cir.setReturnValue(cir.getReturnValue() / zoom.factor());
        }
    }
}
