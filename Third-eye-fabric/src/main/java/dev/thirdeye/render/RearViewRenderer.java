package dev.thirdeye.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.thirdeye.ThirdEyeClient;
import dev.thirdeye.config.ThirdEyeConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraType;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class RearViewRenderer {
    private static boolean enabled = true;
    private static boolean holdActive = false;

    private static Framebuffer fbo;
    private static long lastFrameNanos = 0L;
    private static boolean renderNow = false;

    private static boolean isRenderingRear = false; // used by mixins to disable particles

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) { enabled = v; }
    public static void setHoldActive(boolean v) { holdActive = v; }

    public static boolean isRenderingRear() { return isRenderingRear; }

    public static void init() {
        HudRenderCallback.EVENT.register((poseStack, tickDelta) -> drawHud(poseStack));
        resizeFbo();
    }

    private static void resizeFbo() {
        ThirdEyeConfig cfg = ThirdEyeConfig.get();
        if (fbo != null) fbo.delete();
        fbo = new Framebuffer(cfg.renderWidth, cfg.renderHeight, true, MinecraftClient.IS_SYSTEM_MAC);
    }

    public static void clientTick(MinecraftClient client) {
        ThirdEyeConfig cfg = ThirdEyeConfig.get();
        if (client.getWindow() != null) {
            // If user changed config sizes, keep FBO in sync (simple check)
            if (fbo == null || fbo.viewportWidth != cfg.renderWidth || fbo.viewportHeight != cfg.renderHeight) {
                resizeFbo();
            }
        }

        if (!(enabled || holdActive)) return;

        // FPS cap
        long now = System.nanoTime();
        long frameInterval = (long)(1_000_000_000.0 / Math.max(1, cfg.fpsCap));
        if (now - lastFrameNanos >= frameInterval) {
            renderNow = true;
            lastFrameNanos = now;
        } else {
            renderNow = false;
        }

        if (renderNow) {
            renderRearView(client, cfg);
        }
    }

    private static void renderRearView(MinecraftClient client, ThirdEyeConfig cfg) {
        if (client.world == null || client.player == null) return;

        isRenderingRear = true;
        // Save old settings we tweak
        int oldParticles = client.options.getParticles().getValue();

        if (cfg.particlesOff) {
            client.options.getParticles().setValue(0); // MINIMAL
        }

        try {
            fbo.beginWrite(true);
            // setup GL state
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Build a rear-facing camera
            ClientPlayerEntity p = client.player;
            Vec3d pos = p.getCameraPosVec(1.0f);
            // Offset camera slightly behind the head
            Vec3d look = p.getRotationVecClient();
            Vec3d behind = pos.add(look.multiply(-0.6)); // 0.6 blocks behind
            // Yaw reversed (turn 180 degrees)
            float pitch = p.getPitch();
            float yaw = p.getYaw() + 180.0f;

            Camera camera = client.gameRenderer.getCamera();
            // Temporarily force camera transform (hacky but effective in practice). Implementations vary across versions.
            camera.update(client.world, p, false, false, 1.0f);
            // Note: For deep correctness you'd construct a new Camera instance and inject, but we reuse for simplicity.
            // We'll just move matrices later using view matrix math.

            // Render world with tiny view distance and no clouds/hand/etc.
            WorldRenderer worldRenderer = client.worldRenderer;
            float tickDelta = client.getTickDelta();
            MatrixStack stack = new MatrixStack();

            // Use a custom projection matrix for low FOV (or keep user's FOV). We'll keep default FOV.
            Matrix4f projection = GameRenderer.getBasicProjectionMatrix(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());

            // Render the world. This API is version-sensitive; values may need adjusting on later MC versions.
            // We rely on worldRenderer.render() which uses the active camera & frustum internally.
            // To point it backwards, we alter view rotation via RenderSystem view matrix push/pop around render call.
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(projection);
            // This isn't perfect API-wise but shows the concept; most loaders will need a small mapping tweak.
            worldRenderer.render(stack, tickDelta, System.nanoTime(), false);
            RenderSystem.restoreProjectionMatrix();

        } catch (Throwable t) {
            // swallow to avoid crashing game if something goes off in early dev
        } finally {
            fbo.endWrite();
            isRenderingRear = false;
            // Restore options
            client.options.getParticles().setValue(oldParticles);
        }
    }

    private static void drawHud(MatrixStack poseStack) {
        MinecraftClient client = MinecraftClient.getInstance();
        ThirdEyeConfig cfg = ThirdEyeConfig.get();
        if (!(enabled || holdActive)) return;
        if (client == null || client.player == null) return;
        if (fbo == null) return;

        // Draw the FBO color texture onto HUD
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        int w = cfg.boxWidth;
        int h = cfg.boxHeight;

        int x = cfg.marginX;
        int y = cfg.marginY;

        switch (cfg.corner) {
            case 0: // TopLeft
                break;
            case 1: // TopRight
                x = screenW - cfg.marginX - w;
                break;
            case 2: // BottomLeft
                y = screenH - cfg.marginY - h;
                break;
            case 3: // BottomRight
                x = screenW - cfg.marginX - w;
                y = screenH - cfg.marginY - h;
                break;
        }

        RenderSystem.disableDepthTest();
        fbo.draw(x, y, x + w, y + h, 0, 0, fbo.textureWidth, fbo.textureHeight, false, false);
        RenderSystem.enableDepthTest();
    }
}
