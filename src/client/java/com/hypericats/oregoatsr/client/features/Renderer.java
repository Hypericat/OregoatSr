package com.hypericats.oregoatsr.client.features;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.OptionalDouble;

public class Renderer {

    public static final RenderLayer.MultiPhase ESP_LINES =
            RenderLayer.of("oregoat:esp_lines", 1536, WurstShaderPipelines.ESP_LINES,
                    RenderLayer.MultiPhaseParameters.builder()
                            .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2)))
                            .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
                            .target(RenderLayer.ITEM_ENTITY_TARGET).build(false));


    public static Box getLerpedEntityBoundingBox(Entity entity, float partialTicks) {
        Vec3d lerpedPos = entity.getLerpedPos(partialTicks);

        return entity.getBoundingBox().offset(lerpedPos.x - entity.getX(), lerpedPos.y - entity.getY(), lerpedPos.z - entity.getZ());
    }

    public static void drawSolidBox(Box bb, MatrixStack matrixStack, VertexConsumer buffer, int color) {
        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        buffer.vertex(matrix, minX, minY, minZ);
        buffer.vertex(matrix, maxX, minY, minZ);
        buffer.vertex(matrix, maxX, minY, maxZ);
        buffer.vertex(matrix, minX, minY, maxZ);

        buffer.vertex(matrix, minX, maxY, minZ);
        buffer.vertex(matrix, minX, maxY, maxZ);
        buffer.vertex(matrix, maxX, maxY, maxZ);
        buffer.vertex(matrix, maxX, maxY, minZ);

        buffer.vertex(matrix, minX, minY, minZ);
        buffer.vertex(matrix, minX, maxY, minZ);
        buffer.vertex(matrix, maxX, maxY, minZ);
        buffer.vertex(matrix, maxX, minY, minZ);

        buffer.vertex(matrix, maxX, minY, minZ);
        buffer.vertex(matrix, maxX, maxY, minZ);
        buffer.vertex(matrix, maxX, maxY, maxZ);
        buffer.vertex(matrix, maxX, minY, maxZ);

        buffer.vertex(matrix, minX, minY, maxZ);
        buffer.vertex(matrix, maxX, minY, maxZ);
        buffer.vertex(matrix, maxX, maxY, maxZ);
        buffer.vertex(matrix, minX, maxY, maxZ);

        buffer.vertex(matrix, minX, minY, minZ);
        buffer.vertex(matrix, minX, minY, maxZ);
        buffer.vertex(matrix, minX, maxY, maxZ);
        buffer.vertex(matrix, minX, maxY, minZ);
    }


    // Stolen from wurst as most of this is
    public static void drawOutlinedBox(Box box, MatrixStack matrices, VertexConsumer buffer, int color) {
        MatrixStack.Entry entry = matrices.peek();

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;


        buffer.vertex(entry, maxX, maxY, maxZ).color(color).normal(entry, 1, 0, 0);
        buffer.vertex(entry, minX, maxY, maxZ).color(color).normal(entry, 1, 0, 0);
        buffer.vertex(entry, maxX, maxY, maxZ).color(color).normal(entry, 0, 0, 1);
        buffer.vertex(entry, maxX, maxY, minZ).color(color).normal(entry, 0, 0, 1);
        buffer.vertex(entry, minX, maxY, maxZ).color(color).normal(entry, 0, 0, 1);
        buffer.vertex(entry, minX, maxY, minZ).color(color).normal(entry, 0, 0, 1);
        buffer.vertex(entry, maxX, maxY, minZ).color(color).normal(entry, 1, 0, 0);
        buffer.vertex(entry, minX, maxY, minZ).color(color).normal(entry, 1, 0, 0);

        // top lines
        buffer.vertex(entry, maxX, minY, maxZ).color(color).normal(entry, 1, 0, 0);
        buffer.vertex(entry, minX, minY, maxZ).color(color).normal(entry, 1, 0, 0);
        buffer.vertex(entry, maxX, minY, maxZ).color(color).normal(entry, 0, 0, 1);
        buffer.vertex(entry, maxX, minY, minZ).color(color).normal(entry, 0, 0, 1);
        buffer.vertex(entry, minX, minY, maxZ).color(color).normal(entry, 0, 0, 1);
        buffer.vertex(entry, minX, minY, minZ).color(color).normal(entry, 0, 0, 1);
        buffer.vertex(entry, maxX, minY, minZ).color(color).normal(entry, 1, 0, 0);
        buffer.vertex(entry, minX, minY, minZ).color(color).normal(entry, 1, 0, 0);

        // side lines
        buffer.vertex(entry, maxX, maxY, maxZ).color(color).normal(entry, 0, 1, 0);
        buffer.vertex(entry, maxX, minY, maxZ).color(color).normal(entry, 0, 1, 0);
        buffer.vertex(entry, minX, maxY, maxZ).color(color).normal(entry, 0, 1, 0);
        buffer.vertex(entry, minX, minY, maxZ).color(color).normal(entry, 0, 1, 0);
        buffer.vertex(entry, maxX, maxY, minZ).color(color).normal(entry, 0, 1, 0);
        buffer.vertex(entry, maxX, minY, minZ).color(color).normal(entry, 0, 1, 0);
        buffer.vertex(entry, minX, maxY, minZ).color(color).normal(entry, 0, 1, 0);
        buffer.vertex(entry, minX, minY, minZ).color(color).normal(entry, 0, 1, 0);
    }
}
