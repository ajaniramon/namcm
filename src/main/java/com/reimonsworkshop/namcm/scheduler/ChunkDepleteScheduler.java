package com.reimonsworkshop.namcm.scheduler;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public final class ChunkDepleteScheduler {

    private static final List<Task> TASKS = new ArrayList<>();
    private static final Set<OpKey> IN_FLIGHT = new HashSet<>();

    private static final int BLOCKS_PER_TICK = 1500;
    private static final int PROGRESS_INTERVAL = 20;

    static {
        ServerTickEvents.END_SERVER_TICK.register(ChunkDepleteScheduler::tick);
    }

    private ChunkDepleteScheduler() {}

    public static void schedule(ServerLevel level, ServerPlayer player, ChunkPos chunk, int maxChanges) {
        OpKey key = new OpKey(level.dimension().toString(), chunk.x, chunk.z);

        if (!IN_FLIGHT.add(key)) {
            player.sendSystemMessage(Component.literal("[NAMCM] Already depleting that chunk."));
            return;
        }

        TASKS.add(new Task(level, player.getUUID(), chunk, maxChanges, key));
    }

    private static void tick(MinecraftServer server) {

        Iterator<Task> it = TASKS.iterator();

        while (it.hasNext()) {
            Task t = it.next();

            ServerPlayer player = server.getPlayerList().getPlayer(t.playerId);
            if (player == null || !(player.level() instanceof ServerLevel playerLevel) || playerLevel != t.level) {
                cleanupTask(t, it);
                continue;
            }

            if (!t.level.hasChunk(t.chunk.x, t.chunk.z)) {
                player.sendSystemMessage(Component.literal("[NAMCM] Chunk unloaded. Aborting."));
                cleanupTask(t, it);
                continue;
            }

            int processed = 0;

            while (processed < BLOCKS_PER_TICK && t.cursorY <= t.maxY) {

                BlockPos pos = new BlockPos(t.cursorX, t.cursorY, t.cursorZ);
                processed++;

                if (shouldDelete(t.level, pos)) {

                    if (t.changedTotal >= t.maxChanges) {
                        player.sendSystemMessage(Component.literal(
                                "[NAMCM] Max changes reached (" + t.maxChanges + "). Stopping."
                        ));
                        cleanupTask(t, it);
                        return;
                    }

                    t.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    t.changedTotal++;
                }

                advanceCursor(t);
            }

            t.progressTick++;
            if (t.progressTick >= PROGRESS_INTERVAL) {
                t.progressTick = 0;
                player.sendSystemMessage(Component.literal(
                        "[NAMCM] Depleting... y=" + t.cursorY +
                                " changed=" + t.changedTotal
                ));
            }

            if (t.cursorY > t.maxY) {
                player.sendSystemMessage(Component.literal(
                        "[NAMCM] Done. Total blocks set to air: " + t.changedTotal
                ));
                cleanupTask(t, it);
            }
        }
    }

    private static void cleanupTask(Task t, Iterator<Task> it) {
        IN_FLIGHT.remove(t.key);
        it.remove();
    }

    private static void advanceCursor(Task t) {
        t.cursorX++;
        if (t.cursorX > t.x1) {
            t.cursorX = t.x0;
            t.cursorZ++;
            if (t.cursorZ > t.z1) {
                t.cursorZ = t.z0;
                t.cursorY++;
            }
        }
    }

    private static boolean shouldDelete(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.isAir()) return false;
        if (state.is(Blocks.BEDROCK)) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (state.hasBlockEntity()) return false;

        return true;
    }

    private record OpKey(String dimension, int cx, int cz) {}

    private static final class Task {
        final ServerLevel level;
        final UUID playerId;
        final ChunkPos chunk;
        final int maxChanges;
        final OpKey key;

        final int x0, x1, z0, z1;
        final int minY, maxY;

        int cursorX, cursorY, cursorZ;
        int changedTotal = 0;
        int progressTick = 0;

        Task(ServerLevel level, UUID playerId, ChunkPos chunk, int maxChanges, OpKey key) {
            this.level = level;
            this.playerId = playerId;
            this.chunk = chunk;
            this.maxChanges = maxChanges;
            this.key = key;

            this.x0 = chunk.getMinBlockX();
            this.x1 = chunk.getMaxBlockX();
            this.z0 = chunk.getMinBlockZ();
            this.z1 = chunk.getMaxBlockZ();

            this.minY = level.getMinY();
            this.maxY = level.getMaxY() - 1;

            this.cursorX = x0;
            this.cursorZ = z0;
            this.cursorY = minY;
        }
    }
}