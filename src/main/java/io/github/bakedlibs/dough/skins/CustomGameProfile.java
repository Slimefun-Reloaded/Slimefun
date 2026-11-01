package io.github.bakedlibs.dough.skins;

import java.net.URL;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

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

    void apply(@Nonnull SkullMeta meta) {
        PlayerProfile playerProfile = Bukkit.createPlayerProfile(uuid, PLAYER_NAME);
        PlayerTextures playerTextures = playerProfile.getTextures();
        playerTextures.setSkin(skinUrl);
        playerProfile.setTextures(playerTextures);
        meta.setOwnerProfile(playerProfile);
    }

    void apply(@Nonnull Skull skull) {
        PlayerProfile playerProfile = Bukkit.createPlayerProfile(uuid, PLAYER_NAME);
        PlayerTextures playerTextures = playerProfile.getTextures();
        playerTextures.setSkin(skinUrl);
        playerProfile.setTextures(playerTextures);
        skull.setOwnerProfile(playerProfile);
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
