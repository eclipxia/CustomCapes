package me.eclipxia.customcapes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Capes shared through a public registry (a git repo): an index file maps player UUIDs to png files.
 * Only the index is fetched up front; a player's png is downloaded the first time that player is seen.
 * No player data is ever sent anywhere, the registry is a plain public file.
 *
 * <pre>
 * { "capes": { "00000000-0000-0000-0000-000000000000": "capes/00000000-0000-0000-0000-000000000000.png" } }
 * </pre>
 */
public final class SharedCapes {
	private static final int MAX_PNG_BYTES = 256 * 1024;
	private static final int MAX_INDEX_BYTES = 8 * 1024 * 1024;

	private static final HttpClient HTTP = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	/** uuid -> absolute png url, from the registry index */
	private static volatile Map<UUID, String> index = Map.of();
	/** uuid -> texture handle (see {@link Compat#registerTexture}) */
	private static final Map<UUID, Object> textures = new ConcurrentHashMap<>();
	/** uuids we already started a download for, so a failed download is not retried every frame */
	private static final Set<UUID> requested = ConcurrentHashMap.newKeySet();
	/** bumped on every refresh so results of an older refresh are dropped */
	private static final AtomicInteger generation = new AtomicInteger();

	private SharedCapes() {}

	/** @return the texture handle for this player's registry cape, or null if they have none (yet) */
	public static Object capeFor(UUID player) {
		final Object texture = textures.get(player);
		if (texture != null) {
			return texture;
		}

		final String url = index.get(player);
		if (url != null && requested.add(player)) {
			download(player, url, generation.get());
		}
		return null;
	}

	/** Drops everything loaded so far and re-downloads the index (async). */
	public static void refresh(String indexUrl, java.util.function.Consumer<String> log) {
		final int gen = generation.incrementAndGet();
		index = Map.of();
		requested.clear();
		final Object[] old = textures.values().toArray();
		textures.clear();
		Minecraft.getInstance().execute(() -> {
			for (Object handle : old) {
				Compat.release(handle);
			}
		});

		final URI uri = parseHttps(indexUrl);
		if (uri == null) {
			log.accept("Shared capes: registry url must be a valid https url");
			return;
		}

		HTTP.sendAsync(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build(),
						HttpResponse.BodyHandlers.ofByteArray())
				.thenAccept(response -> {
					if (response.statusCode() != 200 || response.body().length > MAX_INDEX_BYTES) {
						log.accept("Shared capes: could not load registry (HTTP " + response.statusCode() + ")");
						return;
					}
					final Map<UUID, String> parsed = parseIndex(uri, new String(response.body(), java.nio.charset.StandardCharsets.UTF_8));
					if (gen == generation.get()) {
						index = parsed;
						log.accept("Shared capes: " + parsed.size() + " players in the registry");
					}
				})
				.exceptionally(t -> {
					log.accept("Shared capes: could not load registry (" + t.getMessage() + ")");
					return null;
				});
	}

	private static Map<UUID, String> parseIndex(URI base, String json) {
		final Map<UUID, String> result = new ConcurrentHashMap<>();
		try {
			final JsonObject capes = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("capes");
			if (capes == null) {
				return result;
			}
			for (Map.Entry<String, JsonElement> entry : capes.entrySet()) {
				try {
					// paths are relative to the index file, and must stay on https
					final URI resolved = base.resolve(entry.getValue().getAsString());
					if ("https".equalsIgnoreCase(resolved.getScheme())) {
						result.put(UUID.fromString(entry.getKey()), resolved.toString());
					}
				} catch (RuntimeException ignored) {
					// skip malformed entries instead of rejecting the whole registry
				}
			}
		} catch (RuntimeException e) {
			System.err.println("[CustomCapes] Invalid registry index: " + e.getMessage());
		}
		return result;
	}

	private static void download(UUID player, String url, int gen) {
		final URI uri = parseHttps(url);
		if (uri == null) {
			return;
		}

		HTTP.sendAsync(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build(),
						HttpResponse.BodyHandlers.ofByteArray())
				.thenAccept(response -> {
					final byte[] png = response.body();
					if (response.statusCode() != 200 || png.length == 0 || png.length > MAX_PNG_BYTES || !looksLikeSmallPng(png)) {
						return;
					}
					// textures must be created on the render thread
					Minecraft.getInstance().execute(() -> {
						if (gen != generation.get()) {
							return;
						}
						try {
							textures.put(player, Compat.registerTexture("shared/" + player, png));
						} catch (IOException | RuntimeException e) {
							System.err.println("[CustomCapes] Failed to load shared cape for " + player + ": " + e.getMessage());
						}
					});
				})
				.exceptionally(t -> null);
	}

	/** png signature + IHDR width/height sanity check, so a bogus file never reaches the image decoder */
	private static boolean looksLikeSmallPng(byte[] b) {
		if (b.length < 24 || (b[0] & 0xFF) != 0x89 || b[1] != 'P' || b[2] != 'N' || b[3] != 'G') {
			return false;
		}
		final int w = ((b[16] & 0xFF) << 24) | ((b[17] & 0xFF) << 16) | ((b[18] & 0xFF) << 8) | (b[19] & 0xFF);
		final int h = ((b[20] & 0xFF) << 24) | ((b[21] & 0xFF) << 16) | ((b[22] & 0xFF) << 8) | (b[23] & 0xFF);
		return w > 0 && h > 0 && w <= 1024 && h <= 512;
	}

	private static URI parseHttps(String url) {
		try {
			final URI uri = URI.create(url.trim());
			return "https".equalsIgnoreCase(uri.getScheme()) ? uri : null;
		} catch (RuntimeException e) {
			return null;
		}
	}
}
