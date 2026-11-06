package com.feedanimals.mod.client;

import com.feedanimals.mod.network.PhaseNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PhaseConfigScreen extends Screen {
    private EditBox phaseCount;

    public PhaseConfigScreen() {
        super(Component.literal("Configurar fases"));
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        phaseCount = new EditBox(this.font, center - 50, this.height / 2 - 12,
                100, 20, Component.literal("Quantidade de fases"));
        phaseCount.setValue("3");
        phaseCount.setFilter(value -> value.isEmpty() || value.matches("\\d{0,2}"));
        addRenderableWidget(phaseCount);

        addRenderableWidget(Button.builder(Component.literal("Confirmar"), button -> {
            int count;
            try {
                count = Integer.parseInt(phaseCount.getValue());
            } catch (NumberFormatException exception) {
                count = 0;
            }
            if (count < 2 || count > 64) {
                phaseCount.setValue("3");
                return;
            }
            PhaseNetwork.CHANNEL.sendToServer(
                    new PhaseNetwork.SetPhaseCountPacket(count));
            onClose();
        }).bounds(center - 50, this.height / 2 + 18, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, "DELIMITADOR DE FASES",
                width / 2, height / 2 - 55, 0xFFD34A);
        graphics.drawCenteredString(font, "Escolha de 2 a 64 fases",
                width / 2, height / 2 - 30, 0xFFFFFF);
        graphics.drawCenteredString(font, "Depois, clique no primeiro e no último bloco.",
                width / 2, height / 2 + 52, 0xC0C0C0);
    }
}
