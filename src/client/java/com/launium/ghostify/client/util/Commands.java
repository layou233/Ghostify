package com.launium.ghostify.client.util;

import com.launium.ghostify.client.ui.Alignment;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.function.Consumer;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class Commands {
    public static LiteralArgumentBuilder<FabricClientCommandSource> thenAlignment(LiteralArgumentBuilder<FabricClientCommandSource> builder, Consumer<Alignment> callback) {
        return builder
                .then(literal("start")
                        .executes(context -> {
                            callback.accept(Alignment.START);
                            return 0;
                        }))
                .then(literal("end")
                        .executes(context -> {
                            callback.accept(Alignment.END);
                            return 0;
                        }))
                .then(literal("center")
                        .executes(context -> {
                            callback.accept(Alignment.CENTER);
                            return 0;
                        }));
    }
}
