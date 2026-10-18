package de.helden3;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class Helden3 implements ModInitializer {
    public static final String MOD_ID = "helden3";
    public static final int MAX_LIVES = 3;

    public static final AttachmentType<Integer> LIVES = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "lives"),
            builder -> builder
                    .initializer(() -> MAX_LIVES)
                    .persistent(Codec.INT)
                    .copyOnDeath()
                    .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly())
    );

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                int lives = Math.max(0, player.getAttachedOrCreate(LIVES) - 1);
                player.setAttached(LIVES, lives);
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                int lives = newPlayer.getAttachedOrCreate(LIVES);
                if (lives > 0) {
                    newPlayer.sendSystemMessage(Component.literal(
                            "Du hast ein Herz verloren! Übrig: " + lives + " von " + MAX_LIVES));
                }
            }
            enforceSpectator(newPlayer);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                enforceSpectator(handler.getPlayer()));
    }

    private static void enforceSpectator(ServerPlayer player) {
        if (player.getAttachedOrCreate(LIVES) <= 0 && !player.isSpectator()) {
            player.setGameMode(GameType.SPECTATOR);
            player.sendSystemMessage(Component.literal(
                    "Alle Herzen verloren! Du bist jetzt Zuschauer und kannst über die Karte fliegen."));
        }
    }
}
