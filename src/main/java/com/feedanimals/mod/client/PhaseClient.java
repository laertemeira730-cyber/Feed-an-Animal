package com.feedanimals.mod.client;

import net.minecraft.client.Minecraft;

public final class PhaseClient {
    private PhaseClient() {}
    public static void open() {
        Minecraft.getInstance().setScreen(new PhaseConfigScreen());
    }
}
