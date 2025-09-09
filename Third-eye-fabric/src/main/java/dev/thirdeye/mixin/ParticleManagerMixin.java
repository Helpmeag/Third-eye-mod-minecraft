package dev.thirdeye.mixin;

import dev.thirdeye.render.RearViewRenderer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void thirdeye$skipParticles(MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider.Immediate bufferBuilder, Camera camera, float tickDelta, CallbackInfo ci) {
        if (RearViewRenderer.isRenderingRear()) {
            ci.cancel();
        }
    }
}
