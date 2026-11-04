package me.eclipxia.customcapes;

import net.minecraft.client.Minecraft;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.StringSetting;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomCapeModule extends ToggleableModule {
    public static final String DEFAULT_REGISTRY_URL = "https://raw.githubusercontent.com/eclipxia/CustomCapes-Registry/main/capes.json";

    public static CustomCapeModule INSTANCE;

    public StringSetting cape = new StringSetting("Cape", "None");
    public BooleanSetting onlyForMe = new BooleanSetting("Only For Me", "Show the cape only for yourself", true)
            .setVisibility(() -> !cape.getValue().equalsIgnoreCase("None"));

    public BooleanSetting sharedCapes = new BooleanSetting("Shared Capes", "Show the capes other players added to the shared registry", true);
    public StringSetting registryUrl = new StringSetting("Registry URL", "Index file of the shared cape registry (https), reload with *cape reload", DEFAULT_REGISTRY_URL)
            .setVisibility(() -> sharedCapes.getValue());

    public File capesDir = new File(RusherHackAPI.getPath().toFile(), "capes");
    private final Map<String, Object> capeTextures = new HashMap<>();

    public CustomCapeModule() {
        super("CustomCape", "Allows you to use a custom cape", ModuleCategory.RENDER);
        INSTANCE = this;
        registerSettings(cape, onlyForMe, sharedCapes, registryUrl);
        loadCapes();
    }

    public void loadCapes() {
        capeTextures.values().forEach(Compat::release);
        capeTextures.clear();
        cape.getOptions().clear();

        cape.addOptions("None");

        if (sharedCapes.getValue()) {
            SharedCapes.refresh(registryUrl.getValue(), msg -> Minecraft.getInstance().execute(() -> ChatUtils.print(msg)));
        }

        if (!capesDir.exists() && capesDir.mkdirs()) {
            ChatUtils.print("Created capes directory");
        }

        File[] capeFiles = capesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (capeFiles == null || capeFiles.length == 0) {
            return;
        }

        for (File cp : capeFiles) {
            String name = cp.getName().substring(0, cp.getName().length() - 4);
            try {
                // the texture handle type (ResourceLocation / Identifier) differs per Minecraft version
                capeTextures.put(name, Compat.registerTexture(name, Files.readAllBytes(cp.toPath())));
                cape.addOptions(name);
            } catch(Exception exc) {
                System.err.println("Failed to load cape: " + name);
                exc.printStackTrace();
            }
        }

        ChatUtils.print("Loaded " + capeTextures.size() + " capes!");
    }

    /**
     * @return the cape texture handle to apply to the given player, or null to leave their skin untouched.
     * The handle is opaque here; the version specific mixin knows its concrete type.
     */
    public static Object capeFor(UUID playerId) {
        final CustomCapeModule module = INSTANCE;
        if (module == null || !module.isToggled()) {
            return null;
        }

        final Minecraft mc = Minecraft.getInstance();
        final boolean isMe = mc.player != null && mc.player.getUUID().equals(playerId);

        // 1. the cape picked locally
        if (!module.cape.getValue().equalsIgnoreCase("None") && (isMe || !module.onlyForMe.getValue())) {
            final Object local = module.capeTextures.get(module.cape.getValue());
            if (local != null) {
                return local;
            }
        }

        // 2. the cape this player registered in the shared registry
        return module.sharedCapes.getValue() ? SharedCapes.capeFor(playerId) : null;
    }
}
