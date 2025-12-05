package com.launium.ghostify.client.feature;

import com.launium.ghostify.client.annotations.SkipObfuscation;
import com.launium.ghostify.client.config.ConfigManager;
import com.launium.ghostify.client.util.SkyblockLocation;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GoonBlocker implements AttackEntityCallback {
    public static final GoonBlocker INSTANCE = new GoonBlocker();

    private static final String GOON_SKIN = "ewogICJ0aW1lc3RhbXAiIDogMTYxNzI0MTMzMTk4OCwKICAicHJvZmlsZUlkIiA6ICJmMjc0YzRkNjI1MDQ0ZTQxOGVmYmYwNmM3NWIyMDIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeXBpZ3NlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80OWU0ZTQzYjNlMmI5NTc4NTA4NWY0MjJmNGVhODc1YmZmYmNlNzA3MWIxYTc3Nzk0N2YzYzg4M2Q2ZjAxNTVmIgogICAgfQogIH0KfQ==";

    @SkipObfuscation
    @Override
    public InteractionResult interact(Player player, Level level, InteractionHand interactionHand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!ConfigManager.FEATURES.ENABLE_PREVENT_ATTACKING_ON_GOONS) return InteractionResult.PASS;
        if (!SkyblockLocation.LOCATION_STRING.equals("Hub")) return InteractionResult.PASS;
        if (entity instanceof Player targetPlayer) {
            GameProfile profile = targetPlayer.getGameProfile();
            if (profile.name().length() == 10 // NPC always has a name consists of 10 random characters
                    && profile.properties().get("textures").stream()
                    .filter(Objects::nonNull)
                    .map(Property::value)
                    .anyMatch(it -> it.equals(GOON_SKIN))
            ) {
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }
}
