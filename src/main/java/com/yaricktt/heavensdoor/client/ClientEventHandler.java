package com.yaricktt.heavensdoor.client;

import net.minecraft.client.Minecraft;

public class ClientEventHandler {
    private static ClientEventHandler instance = null;
    private final Minecraft mc;
    private ClientEventHandler(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
//        if (instance == null) {
//            instance = new ClientEventHandler(mc);
//            MinecraftForge.EVENT_BUS.register(instance);
//        }
    }

}