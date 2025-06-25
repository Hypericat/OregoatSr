package com.hypericats.oregoatsr.client.features;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class EntityEsp {

    public static Stream<Entity> filterEntityList(Stream<Entity> entities) {
        return entities.filter(e -> e.getId() != MinecraftClient.getInstance().player.getId());
    }

    public static void onRender(MatrixStack matrixStack, float partialTicks) {
        if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) return;

        List<Entity> entities = new ArrayList<>();
        MinecraftClient.getInstance().world.getEntities().forEach(entities::add);

        for (Entity entity : filterEntityList(entities.stream()).toList()) {

            Camera camera = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().camera;

            matrixStack.push();
            matrixStack.translate(camera.getPos().negate());

            VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

            Renderer.drawOutlinedBox(Renderer.getLerpedEntityBoundingBox(entity, partialTicks), matrixStack, consumer.getBuffer(Renderer.ESP_LINES), 0xff00ff00);
            consumer.draw(Renderer.ESP_LINES);

            matrixStack.pop();
        }
    }
}
