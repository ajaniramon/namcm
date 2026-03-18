package com.reimonsworkshop.namcm.snapshot;

import com.google.gson.stream.JsonWriter;
import com.reimonsworkshop.namcm.util.MessageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public final class ChunkSnapshotDumper {

    private ChunkSnapshotDumper() {}

    public static Path dump(ServerPlayer player, ServerLevel level, ChunkPos cp) throws Exception {
        // Asegura que está cargado (esto puede cargarlo si no lo está)
        LevelChunk chunk = level.getChunk(cp.x, cp.z);

        Path dir = Paths.get("namcm", "snapshots");
        Files.createDirectories(dir);

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path out = dir.resolve("chunk_" + cp.x + "_" + cp.z + "_" + ts + ".json.gz");

        int minY = level.getMinY();
        int maxY = level.getMaxY() - 1;

        // Para biomas: muestreamos a “altura típica”
        int biomeY = clamp(level.getSeaLevel(), minY, maxY);

        int x0 = cp.getMinBlockX();
        int x1 = cp.getMaxBlockX();
        int z0 = cp.getMinBlockZ();
        int z1 = cp.getMaxBlockZ();

        try (var fos = Files.newOutputStream(out, StandardOpenOption.CREATE_NEW);
             var gzs = new GZIPOutputStream(fos);
             var osw = new OutputStreamWriter(gzs, StandardCharsets.UTF_8);
             var bw  = new BufferedWriter(osw);
             var jw  = new JsonWriter(bw)) {

            jw.setIndent("  ");

            jw.beginObject();

            jw.name("schema").value(1);
            jw.name("dimension").value(level.dimension().identifier().toString());
            jw.name("chunkX").value(cp.x);
            jw.name("chunkZ").value(cp.z);
            jw.name("minY").value(minY);
            jw.name("maxY").value(maxY);
            jw.name("biomeSampleY").value(biomeY);

            // ---- BIOMES (por columna x,z)
            jw.name("biomes");
            jw.beginArray();

            for (int z = z0; z <= z1; z++) {
                for (int x = x0; x <= x1; x++) {
                    BlockPos p = new BlockPos(x, biomeY, z);
                    String biomeId = biomeId(level, p);

                    jw.beginObject();
                    jw.name("x").value(x);
                    jw.name("z").value(z);
                    jw.name("biome").value(biomeId);
                    jw.endObject();
                }
            }

            jw.endArray();

            // ---- BLOCKS (solo no-air)
            jw.name("blocks");
            jw.beginArray();

            for (int y = minY; y <= maxY; y++) {
                for (int z = z0; z <= z1; z++) {
                    for (int x = x0; x <= x1; x++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState st = chunk.getBlockState(pos);

                        if (st.isAir()) continue;

                        jw.beginObject();
                        jw.name("x").value(x);
                        jw.name("y").value(y);
                        jw.name("z").value(z);
                        jw.name("state").value(blockStateToString(st));
                        jw.endObject();
                    }
                }
            }

            jw.endArray();

            jw.endObject();
        }

        MessageUtil.sendToPlayer(player, "[NAMCM] Dump complete.");
        return out;
    }

    private static String biomeId(ServerLevel level, BlockPos pos) {
        var holder = level.getBiome(pos);
        var keyOpt = holder.unwrapKey();

        if (keyOpt.isPresent()) {
            return keyOpt.get().identifier().toString();
        }

        throw new RuntimeException("cannot found biome for pos: " + pos);
    }

    private static String blockStateToString(BlockState st) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(st.getBlock());

        Map<Property<?>, Comparable<?>> values = st.getValues();
        if (values.isEmpty()) {
            return id.toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(id);

        sb.append('[');

        values.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getName()))
                .forEachOrdered(e -> {
                    Property<?> prop = e.getKey();
                    Comparable<?> val = e.getValue();
                    sb.append(prop.getName())
                            .append('=')
                            .append(valueToString(prop, val))
                            .append(',');
                });

        // quitar coma final
        sb.setLength(sb.length() - 1);

        sb.append(']');
        return sb.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String valueToString(Property prop, Comparable val) {
        return prop.getName(val);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}