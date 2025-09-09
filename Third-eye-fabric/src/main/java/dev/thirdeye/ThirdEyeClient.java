package dev.thirdeye;

import dev.thirdeye.config.ThirdEyeConfig;
import dev.thirdeye.render.RearViewRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ThirdEyeClient implements ClientModInitializer {
    public static final String MOD_ID = "thirdeye";

    private static KeyBinding TOGGLE_KEY;
    private static KeyBinding HOLD_KEY;

    @Override
    public void onInitializeClient() {
        ThirdEyeConfig.load(); // load config once

        TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.thirdeye.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.thirdeye"
        ));

        HOLD_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.thirdeye.hold",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category.thirdeye"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_KEY.wasPressed()) {
                RearViewRenderer.setEnabled(!RearViewRenderer.isEnabled());
            }
            RearViewRenderer.setHoldActive(HOLD_KEY.isPressed());
            RearViewRenderer.clientTick(client);
        });

        RearViewRenderer.init(); // setup FBO etc
    }
}
