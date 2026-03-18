package com.reimonsworkshop.namcm.item;

import com.reimonsworkshop.namcm.snapshot.ChunkSnapshotDumper;
import com.reimonsworkshop.namcm.util.MessageUtil;
import com.reimonsworkshop.namcm.scheduler.ParticleBorderScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;

public class ChunkProbeItem extends Item {

    public ChunkProbeItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }

        if (!(ctx.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }

        final BlockPos clicked = ctx.getClickedPos();
        final ChunkPos chunk = new ChunkPos(clicked);

        MessageUtil.sendToPlayer(player,"[NAMCM] Chunk: " + chunk.x + ", " + chunk.z);

        this.paintChunkBorderParticles(level, chunk, player);

        if (player.isShiftKeyDown()) {
            this.dumpChunk(level, player, chunk);
        }

        return InteractionResult.CONSUME;
    }

    private void dumpChunk(
            final ServerLevel level,
            final ServerPlayer player,
            final ChunkPos chunk
    ) {
        MessageUtil.sendToPlayer(player,"[NAMCM] Dumping chunk...");

        try {
            ChunkSnapshotDumper.dump(player, level, chunk);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void paintChunkBorderParticles(
            final ServerLevel level,
            final ChunkPos chunk,
            final ServerPlayer player
    ) {
        double step = 1.0;

        int durationTicks = 60;
        ParticleBorderScheduler.schedule(level, player, chunk, player.getOnPos().getY()+1, step, durationTicks);
    }
}