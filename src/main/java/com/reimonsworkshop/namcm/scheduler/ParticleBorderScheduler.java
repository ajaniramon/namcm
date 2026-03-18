package com.reimonsworkshop.namcm.scheduler;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class ParticleBorderScheduler {

    private static final List<Task> TASKS = new ArrayList<>();

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Task> it = TASKS.iterator();
            while (it.hasNext()) {
                Task t = it.next();
                t.ticksLeft--;

                ServerPlayer player = server.getPlayerList().getPlayer(t.playerId);
                if (player == null || !(player.level() instanceof ServerLevel playerLevel)) {
                    it.remove();
                    continue;
                }

                if (playerLevel != t.level) {
                    it.remove();
                    continue;
                }

                if ((t.ticksLeft & 1) == 0) {
                    drawBorderParticles(t.level, player, t.chunk, t.y, t.step);
                }

                if (t.ticksLeft <= 0) {
                    it.remove();
                }
            }
        });
    }

    private ParticleBorderScheduler() {}

    public static void schedule(ServerLevel level, ServerPlayer player, ChunkPos chunk, int y, double step, int durationTicks) {
        TASKS.add(new Task(level, player.getUUID(), chunk, y, step, durationTicks));
    }

    private static void drawBorderParticles(ServerLevel level, ServerPlayer player, ChunkPos chunk, int y, double step) {
        int x0 = chunk.getMinBlockX();
        int x1 = chunk.getMaxBlockX() + 1;
        int z0 = chunk.getMinBlockZ();
        int z1 = chunk.getMaxBlockZ() + 1;

        var p = ParticleTypes.END_ROD;

        for (double x = x0; x <= x1; x += step) {
            spawnToPlayer(level, player, p, x + 0.5, y + 0.2, z0 + 0.5);
            spawnToPlayer(level, player, p, x + 0.5, y + 0.2, z1 + 0.5);
        }


        for (double z = z0; z <= z1; z += step) {
            spawnToPlayer(level, player, p, x0 + 0.5, y + 0.2, z + 0.5);
            spawnToPlayer(level, player, p, x1 + 0.5, y + 0.2, z + 0.5);
        }
    }

    private static void spawnToPlayer(ServerLevel level, ServerPlayer player,
                                      ParticleOptions particle,
                                      double x, double y, double z) {

        boolean force = true;
        boolean overrideLimiter = false;

        int count = 1;
        double dx = 0.0, dy = 0.0, dz = 0.0;
        double speed = 0.0;

        level.sendParticles(player, particle, force, overrideLimiter,
                x, y, z, count, dx, dy, dz, speed);
    }

    private static final class Task {
        final ServerLevel level;
        final UUID playerId;
        final ChunkPos chunk;
        final int y;
        final double step;
        int ticksLeft;

        Task(ServerLevel level, UUID playerId, ChunkPos chunk, int y, double step, int ticksLeft) {
            this.level = level;
            this.playerId = playerId;
            this.chunk = chunk;
            this.y = y;
            this.step = step;
            this.ticksLeft = ticksLeft;
        }
    }
}