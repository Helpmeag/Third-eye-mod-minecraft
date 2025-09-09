package dev.thirdeye.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ThirdEyeConfig {
    public boolean enabled = true;
    public int boxWidth = 256;       // UI box size (rendered texture gets upscaled)
    public int boxHeight = 144;
    public int renderWidth = 320;    // internal FBO size for rear view (low res for perf)
    public int renderHeight = 180;
    public int fpsCap = 30;          // rear view fps
    public int chunkDistance = 4;    // tiny view dist
    public int marginX = 12;         // HUD margin
    public int marginY = 12;
    public int corner = 1;           // 0=TopLeft, 1=TopRight, 2=BottomLeft, 3=BottomRight
    public boolean particlesOff = true;
    public boolean entitiesOnly = true; // try to avoid block entities where possible

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ThirdEyeConfig INSTANCE;

    public static ThirdEyeConfig get() {
        if (INSTANCE == null) INSTANCE = new ThirdEyeConfig();
        return INSTANCE;
    }

    public static void load() {
        try {
            File cfgDir = new File(MinecraftClient.getInstance().runDirectory, "config");
            if (!cfgDir.exists()) cfgDir.mkdirs();
            File file = new File(cfgDir, "thirdeye.json");
            if (file.exists()) {
                try (FileReader r = new FileReader(file)) {
                    INSTANCE = GSON.fromJson(r, ThirdEyeConfig.class);
                }
            } else {
                INSTANCE = new ThirdEyeConfig();
                save();
            }
        } catch (Exception e) {
            INSTANCE = new ThirdEyeConfig();
        }
    }

    public static void save() {
        try {
            File cfgDir = new File(MinecraftClient.getInstance().runDirectory, "config");
            if (!cfgDir.exists()) cfgDir.mkdirs();
            File file = new File(cfgDir, "thirdeye.json");
            try (FileWriter w = new FileWriter(file)) {
                GSON.toJson(get(), w);
            }
        } catch (Exception ignored) {}
    }
}
