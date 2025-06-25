package com.hypericats.oregoatsr.client.features;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

import java.util.HashMap;
import java.util.HashSet;

public class BlockEsp {

    private static final HashMap<Long, HashSet<BlockPos>> blocks = new HashMap<>();

    public static boolean shouldESPBlock(BlockState block) {
        return block.getBlock() == Blocks.LAVA;
    }

    public static void onRender(MatrixStack matrixStack, float partialTicks) {
        Vec3d cameraPos = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().camera.getPos().negate();

        matrixStack.push();
        matrixStack.translate(cameraPos);

        blocks.values().forEach(b -> b.forEach(pos -> renderPos(pos, matrixStack)));

        matrixStack.pop();
    }

    public static void renderPos(BlockPos pos, MatrixStack matrixStack) {
        Box box = new Box(pos);
        VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        Renderer.drawOutlinedBox(box, matrixStack, consumer.getBuffer(Renderer.ESP_LINES), 0xff0000ff);
        consumer.draw(Renderer.ESP_LINES);
    }

    public static Long getChunkHash(int x, int z) {
        return ((long) x) | (((long) z) << 16);
    }


    public static Long getChunkHash(ChunkPos chunkPos) {
        return getChunkHash(chunkPos.x, chunkPos.z);
    }

    public static void onChunkLoad(Chunk chunk) {
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
        blocks.remove(getChunkHash(chunk.getPos()));
    }

    public static void onPacket(Packet<?> packet) {
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





}
