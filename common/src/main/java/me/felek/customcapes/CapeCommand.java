package me.felek.customcapes;

import net.minecraft.client.Minecraft;
import org.rusherhack.core.command.AbstractCommand;
import org.rusherhack.core.command.annotations.CommandExecutor;

public class CapeCommand extends AbstractCommand {
    public CapeCommand() {
        super("cape", "Manages custom capes.");
    }

    @CommandExecutor(subCommand = "reload")
    private String reloadCapes() {
        if (CustomCapeModule.INSTANCE != null) {
            CustomCapeModule.INSTANCE.loadCapes();
            return "Reloaded capes from the directory and the shared registry.";
        }

        return "CustomCape module not found or not loaded yet.";
    }

    @CommandExecutor(subCommand = "uuid")
    private String uuid() {
        final Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? "Join a world first." : "Your UUID (use it to add your cape to the registry): " + mc.player.getUUID();
    }
}
