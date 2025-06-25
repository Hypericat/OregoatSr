package com.hypericats.oregoatsr.client.features;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class BlockEsp {
    private static boolean enabled;
    private static boolean showTracer;
    private static boolean showOutline = true;
    private final static HashSet<String> filterNames = new HashSet<>();

    private static final HashMap<Long, HashSet<BlockPos>> blocks = new HashMap<>();

    public static boolean shouldESPBlock(BlockState block) {
        return filterNames.contains(block.getBlock().getName().getString().toLowerCase());
    }

    public static void onRender(MatrixStack matrixStack, float partialTicks) {
        if (!isEnabled() || (!showOutline && !showTracer)) return;

        Vec3d cameraPos = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().camera.getPos().negate();
        blocks.values().forEach(b -> b.forEach(pos -> renderPos(pos, matrixStack, partialTicks, cameraPos)));
    }

    public static void renderPos(BlockPos pos, MatrixStack matrixStack, float partialTicks, Vec3d cameraPos) {
        Box box = new Box(pos);
        VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        if (showOutline) {
            matrixStack.push();
            matrixStack.translate(cameraPos);

            Renderer.drawOutlinedBox(box, matrixStack, consumer.getBuffer(Renderer.ESP_LINES), 0xff0000ff);
            consumer.draw(Renderer.ESP_LINES);

            matrixStack.pop();
        }

        if (showTracer) {
            matrixStack.push();

            Renderer.renderTracer(box.getCenter(), matrixStack, consumer.getBuffer(Renderer.ESP_LINES), 0xffff0000, partialTicks, cameraPos);
            consumer.draw(Renderer.ESP_LINES);
            matrixStack.pop();
        }
    }

    public static Long getChunkHash(int x, int z) {
        return ((long) x) | (((long) z) << 16);
    }


    public static Long getChunkHash(ChunkPos chunkPos) {
        return getChunkHash(chunkPos.x, chunkPos.z);
    }

    public static void onChunkLoad(Chunk chunk) {
        if (!isEnabled()) return;

        long hash = getChunkHash(chunk.getPos());

        blocks.remove(hash); // Shouldn't be needed but just to be safe

        HashSet<BlockPos> positions = new HashSet<>();

        chunk.forEachBlockMatchingPredicate(BlockEsp::shouldESPBlock, (blockPos, blockState) -> positions.add(blockPos.toImmutable()));

        if (!positions.isEmpty()) {
            blocks.put(hash, positions);
        }
    }

    public static void onWorldLoad() {
        blocks.clear();
    }

    public static void onChunkUnload(Chunk chunk) {
        if (!isEnabled()) return;
        blocks.remove(getChunkHash(chunk.getPos()));
    }

    public static void onPacket(Packet<?> packet) {
        if (!isEnabled()) return;

        if (packet instanceof BlockUpdateS2CPacket updatePacket) {
            handleBlockChange(updatePacket.getPos(), updatePacket.getState());
            return;
        }

        if (packet instanceof ChunkDeltaUpdateS2CPacket sectionUpdatePacket) {
            sectionUpdatePacket.visitUpdates(BlockEsp::handleBlockChange);
            return;
        }
    }
    
    public static void handleBlockChange(BlockPos pos, BlockState state) {
        long chunkHash = (pos.getX() >> 4) | ((pos.getZ() & 0xFFF0) << 12); //Small optimizations

        if (shouldESPBlock(state)) {
            if (!blocks.containsKey(chunkHash)) {
                blocks.put(chunkHash, new HashSet<>());
            }

            blocks.get(chunkHash).add(pos.toImmutable());
            return;
        }

        if (!blocks.containsKey(chunkHash)) return;
        blocks.get(chunkHash).remove(pos);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;

        if (!b) {
            onWorldLoad();
        }
    }


    public static boolean isShowTracer() {
        return showTracer;
    }

    public static void setShowTracer(boolean showTracer) {
        BlockEsp.showTracer = showTracer;
    }

    public static boolean isShowOutline() {
        return showOutline;
    }

    public static void setShowOutline(boolean showOutline) {
        BlockEsp.showOutline = showOutline;
    }

    public static void setNameFilter(String string) {
        filterNames.clear();
        if (string.isEmpty()) return;
        filterNames.addAll(Arrays.asList(string.split(";")));
    }

    public static String getNameFilter() {
        if (filterNames.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        filterNames.forEach(name -> builder.append(name).append(";"));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
