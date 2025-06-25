package com.hypericats.oregoatsr.client;

import com.hypericats.oregoatsr.client.config.Oreconfig;
import com.hypericats.oregoatsr.client.features.BlockEsp;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;

public class OregoatsrClient implements ClientModInitializer {

    public static final String MOD_ID = "oregoatsr";

    @Override
    public void onInitializeClient() {
        Oreconfig.init();

        ClientChunkEvents.CHUNK_UNLOAD.register((clientWorld, chunk) -> {
            BlockEsp.onChunkUnload(chunk);
        });

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((clientWorld, chunk) -> {
            BlockEsp.onWorldLoad();
        });
    }
}
