package com.feedanimals.mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ManagementSectionScreen extends Screen {
    private final ManagementMenuScreen.Section section;

    ManagementSectionScreen(ManagementMenuScreen.Section section) {
        super(Component.literal(title(section)));
        this.section = section;
    }

    @Override
    protected void init() {
        int center = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Voltar ao painel"),
                b -> Minecraft.getInstance().setScreen(new ManagementMenuScreen()))
                .bounds(center - 75, height / 2 + 48, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = width / 2 - 155;
        int top = height / 2 - 78;
        graphics.fill(left, top, left + 310, top + 160, 0xE8202020);
        graphics.fill(left, top, left + 310, top + 2, 0xFFFFD34A);
        graphics.fill(left, top + 158, left + 310, top + 160, 0xFFFFD34A);
        graphics.drawCenteredString(font, title(section).toUpperCase(), width / 2,
                top + 16, 0xFFFFD34A);
        String[] lines = description(section);
        for (int i = 0; i < lines.length; i++) {
            graphics.drawCenteredString(font, lines[i], width / 2, top + 48 + i * 13, 0xFFFFFFFF);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static String title(ManagementMenuScreen.Section section) {
        return switch (section) {
            case ANIMALS -> "Animais";
            case PEN -> "Cercado";
            case SALES -> "Vendas";
            case TRAINING -> "Treinamento";
            case PHASES -> "Fases";
        };
    }

    private static String[] description(ManagementMenuScreen.Section section) {
        return switch (section) {
            case ANIMALS -> new String[] {
                    "Os animais usam os mobs e modelos originais do Minecraft.",
                    "Esta tela será ligada à lista de animais do seu mundo."
            };
            case PEN -> new String[] {
                    "Use o Delimitador de Cercado para marcar os cantos da área.",
                    "A gestão dos animais dentro do cercado será exibida aqui."
            };
            case SALES -> new String[] {
                    "Use o Delimitador de Vendas para definir a área de venda.",
                    "O resumo das vendas e o saldo serão exibidos aqui."
            };
            case TRAINING -> new String[] {
                    "Treine usando a Barra de Treinamento no mundo.",
                    "Sua força de arremesso atual e sua evolução aparecerão aqui."
            };
            case PHASES -> new String[] {
                    "Configure o Delimitador de Fases e marque os dois extremos.",
                    "As fases, suas divisões e a conclusão individual serão exibidas aqui."
            };
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
