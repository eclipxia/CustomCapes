package me.felek.customcapes;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Version specific calls for 26.2. */
public final class Compat {
	private Compat() {}

	/** Registers the png bytes as a dynamic texture and returns its {@link Identifier} handle. */
	public static Object registerTexture(String name, byte[] png) throws IOException {
		try (InputStream in = new ByteArrayInputStream(png)) {
			final Identifier id = Identifier.fromNamespaceAndPath(
					"custom_capes",
					name.toLowerCase().replaceAll("[^a-z0-9/._-]", "_")
			);
			final DynamicTexture texture = new DynamicTexture(() -> "custom_capes/" + id.getPath(), NativeImage.read(in));
			Minecraft.getInstance().getTextureManager().register(id, texture);
			return id;
		}
	}

	/** Frees a texture returned by {@link #registerTexture}. */
	public static void release(Object handle) {
		if (handle instanceof Identifier id) {
			Minecraft.getInstance().getTextureManager().release(id);
		}
	}
}
