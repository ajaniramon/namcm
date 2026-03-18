package com.reimonsworkshop.namcm.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class MessageUtil {
    public static void sendToPlayer(final ServerPlayer player, final String message) {
        player.sendSystemMessage(Component.literal(message));
    }

}
