package com.reimonsworkshop.namcm.item;

import com.reimonsworkshop.namcm.scheduler.ChunkDepleteScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;

public class ChunkDepleterItem extends Item {

    public ChunkDepleterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!(ctx.getPlayer() instanceof ServerPlayer player)) return InteractionResult.SUCCESS;

        if(!player.isCreative()) {
            player.sendSystemMessage(Component.literal("[NAMCM] Chunk depletion is only available in creative mode."));
            return InteractionResult.CONSUME;
        }

        if (!player.isShiftKeyDown()) {
            player.sendSystemMessage(Component.literal("[NAMCM] Hold SHIFT to deplete a chunk (safety)."));
            return InteractionResult.CONSUME;
        }

        BlockPos clicked = ctx.getClickedPos();
        ChunkPos chunk = new ChunkPos(clicked);

        player.sendSystemMessage(Component.literal(
                "[NAMCM] Depleting chunk (" + chunk.x + ", " + chunk.z + ")..."
        ));

        ChunkDepleteScheduler.schedule(level, player, chunk, 150_000);

        return InteractionResult.CONSUME;
    }
}