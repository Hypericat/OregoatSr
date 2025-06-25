package com.hypericats.oregoatsr.client.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Stream;

public class EntityEsp {
    private static boolean enabled;
    private static boolean showTracer;
    private static boolean showOutline = true;
    private final static HashSet<String> entityTypes = new HashSet<>();
    private final static List<String> entityNames = new ArrayList<>();



    public static Stream<Entity> filterEntityList(Stream<Entity> entities) {
        return entities.filter(e -> (entityTypes.isEmpty() || entityTypes.contains(e.getType().getName().getString().toLowerCase())) && e.getId() != MinecraftClient.getInstance().player.getId() && (entityNames.isEmpty() || entityNames.stream().anyMatch(s -> s.contains(e.getName().getString().toLowerCase()))));
    }

    public static void onRender(MatrixStack matrixStack, float partialTicks) {
        if (!enabled || (!showOutline && !showTracer)) return;

        if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) return;

        List<Entity> entities = new ArrayList<>();
        MinecraftClient.getInstance().world.getEntities().forEach(entities::add);

        VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        Vec3d cameraPos = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().camera.getPos().negate();


        for (Entity entity : filterEntityList(entities.stream()).toList()) {

            Box entityBox = Renderer.getLerpedEntityBoundingBox(entity, partialTicks);

            if (showOutline) {
                matrixStack.push();
                matrixStack.translate(cameraPos);

                Renderer.drawOutlinedBox(entityBox, matrixStack, consumer.getBuffer(Renderer.ESP_LINES), 0xff00ff00);
                consumer.draw(Renderer.ESP_LINES);
                matrixStack.pop();
            }

            if (showTracer) {
                matrixStack.push();

                Renderer.renderTracer(entityBox.getCenter(), matrixStack, consumer.getBuffer(Renderer.ESP_LINES), 0xffff0000, partialTicks, cameraPos);
                consumer.draw(Renderer.ESP_LINES);
                matrixStack.pop();
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        EntityEsp.enabled = enabled;
    }

    public static boolean isShowTracer() {
        return showTracer;
    }

    public static void setShowTracer(boolean showTracer) {
        EntityEsp.showTracer = showTracer;
    }

    public static boolean isShowOutline() {
        return showOutline;
    }

    public static void setShowOutline(boolean showOutline) {
        EntityEsp.showOutline = showOutline;
    }

    public static void setEntityTypeFilter(String string) {
        entityTypes.clear();
        if (string.isEmpty()) return;
        entityTypes.addAll(Arrays.asList(string.split(";")));
    }

    public static String getEntityTypeFilter() {
        if (entityTypes.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        entityTypes.forEach(name -> builder.append(name).append(";"));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static void setEntityNameFilter(String string) {
        entityNames.clear();
        if (string.isEmpty()) return;
        entityNames.addAll(Arrays.asList(string.split(";")));
    }

    public static String getEntityNameFilter() {
        if (entityNames.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        entityNames.forEach(name -> builder.append(name).append(";"));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
