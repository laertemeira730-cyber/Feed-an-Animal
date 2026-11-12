package com.feedanimals.mod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AnimalInfoScreen extends Screen {
    private final String animalName;
    private final float health;
    private final float maxHealth;

    public AnimalInfoScreen(String animalName, float health, float maxHealth) {
        super(Component.literal("Animal"));
        this.animalName = animalName;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> onClose())
                .bounds(width / 2 - 60, height / 2 + 45, 120, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = width / 2 - 145;
        int top = height / 2 - 65;
        graphics.fill(left, top, left + 290, top + 130, 0xE8172638);
        graphics.fill(left, top, left + 290, top + 3, 0xFFFFD34A);
        graphics.drawCenteredString(font, animalName, width / 2, top + 22, 0xFFFFD34A);
        graphics.drawCenteredString(font, "Vida: " + Math.round(health) + "/" + Math.round(maxHealth),
                width / 2, top + 52, 0xFFFFFFFF);
        graphics.drawCenteredString(font, "Modelo e aparência originais do Minecraft",
                width / 2, top + 70, 0xFFBFD7F0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
