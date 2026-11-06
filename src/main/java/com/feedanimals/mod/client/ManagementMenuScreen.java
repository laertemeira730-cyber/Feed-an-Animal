package com.feedanimals.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ManagementMenuScreen extends Screen {
    private static final int GOLD = 0xFFFFD34A;
    private static final int TEXT = 0xFFFFFFFF;

    public ManagementMenuScreen() {
        super(Component.literal("Lançar Comidas para Animais"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new ManagementMenuScreen());
    }

    @Override
    protected void init() {
        int center = width / 2;
        int top = height / 2 - 36;
        int buttonWidth = 150;
        int gap = 10;
        int left = center - buttonWidth - gap / 2;
        int right = center + gap / 2;

        addRenderableWidget(Button.builder(Component.literal("Animais"), b -> openSection(Section.ANIMALS))
                .bounds(left, top, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cercado"), b -> openSection(Section.PEN))
                .bounds(right, top, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vendas"), b -> openSection(Section.SALES))
                .bounds(left, top + 26, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Treinamento"), b -> openSection(Section.TRAINING))
                .bounds(right, top + 26, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fases"), b -> openSection(Section.PHASES))
                .bounds(center - 75, top + 52, buttonWidth, 20).build());
    }

    private void openSection(Section section) {
        Minecraft.getInstance().setScreen(new ManagementSectionScreen(section));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        drawPanel(graphics);
        graphics.drawCenteredString(font, "LANÇAR COMIDAS PARA ANIMAIS", width / 2,
                height / 2 - 82, GOLD);
        graphics.drawCenteredString(font, "PAINEL DE GERENCIAMENTO", width / 2,
                height / 2 - 66, TEXT);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, "Selecione um sistema", width / 2,
                height / 2 + 88, 0xFFBDBDBD);
    }

    private void drawPanel(GuiGraphics graphics) {
        int left = width / 2 - 165;
        int top = height / 2 - 96;
        graphics.fill(left, top, left + 330, top + 198, 0xE8202020);
        graphics.fill(left, top, left + 330, top + 2, GOLD);
        graphics.fill(left, top + 196, left + 330, top + 198, GOLD);
        graphics.fill(left, top, left + 2, top + 198, GOLD);
        graphics.fill(left + 328, top, left + 330, top + 198, GOLD);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    enum Section { ANIMALS, PEN, SALES, TRAINING, PHASES }
}
