package io.github.bakedlibs.dough.skins;

import java.net.URL;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import io.github.bakedlibs.dough.reflection.ReflectionUtils;
import io.github.bakedlibs.dough.versions.MinecraftVersion;
import io.github.bakedlibs.dough.versions.UnknownServerVersionException;

public final class CustomGameProfile {

    /**
     * The player name for this profile.
     * "CS-CoreLib" for historical reasons and backwards compatibility.
     */
    private static final String PLAYER_NAME = "CS-CoreLib";

    /**
     * The skin's property key.
     */
    private static final String PROPERTY_KEY = "textures";

    private final UUID uuid;
    private final URL skinUrl;
    private final String texture;

    CustomGameProfile(@Nonnull UUID uuid, @Nullable String texture, @Nonnull URL url) {
        this.uuid = uuid;
        this.skinUrl = url;
        this.texture = texture;
    }

    void apply(@Nonnull SkullMeta meta) throws NoSuchFieldException, IllegalAccessException, UnknownServerVersionException {
        // setOwnerProfile was added in 1.18, but getOwningPlayer throws a NullPointerException since 1.20.2.
        if (MinecraftVersion.get().isAtLeast(MinecraftVersion.parse("1.20"))) {
            PlayerProfile playerProfile = Bukkit.createPlayerProfile(uuid, PLAYER_NAME);
            PlayerTextures playerTextures = playerProfile.getTextures();
            playerTextures.setSkin(skinUrl);
            playerProfile.setTextures(playerTextures);
            meta.setOwnerProfile(playerProfile);
        } else {
            GameProfile gameProfile = asGameProfile();

            // Forces SkullMeta to properly deserialize and serialize the profile.
            ReflectionUtils.setFieldValue(meta, "profile", gameProfile);
            meta.setOwningPlayer(meta.getOwningPlayer());

            // Now override the texture again.
            ReflectionUtils.setFieldValue(meta, "profile", gameProfile);
        }
    }

    GameProfile asGameProfile() {
        GameProfile gameProfile = new GameProfile(uuid, PLAYER_NAME);

        if (texture != null) {
            gameProfile.getProperties().put(PROPERTY_KEY, new Property(PROPERTY_KEY, texture));
        }

        return gameProfile;
    }

    public UUID getId() {
        return uuid;
    }

    public String getName() {
        return PLAYER_NAME;
    }

    /**
     * Get the base64 encoded texture from the underlying GameProfile-compatible data.
     *
     * @return the base64 encoded texture.
     */
    @Nullable
    public String getBase64Texture() {
        return texture;
    }
}
