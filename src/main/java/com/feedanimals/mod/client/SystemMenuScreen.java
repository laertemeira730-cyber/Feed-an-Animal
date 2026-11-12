package com.feedanimals.mod.client;

import com.feedanimals.mod.network.GameNetwork;
import com.feedanimals.mod.registry.ModItems;
import com.feedanimals.mod.util.EconomyRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SystemMenuScreen extends Screen {
    public enum MenuType { PEN, SALES, TRAINING, SHOP, PHASES }

    private static final int GOLD = 0xFFFFD34A;
    private static final int BLUE_PANEL = 0xE8172638;
    private final MenuType type;
    private final BlockPos trainingPos;
    private int phasePage;
    private int activePhase;
    private int lastPhaseCount = -1;

    public SystemMenuScreen(MenuType type) {
        this(type, BlockPos.ZERO);
    }

    public SystemMenuScreen(MenuType type, BlockPos trainingPos) {
        super(Component.literal(title(type)));
        this.type = type;
        this.trainingPos = trainingPos.immutable();
    }

    public static void open(MenuType type) {
        Minecraft.getInstance().setScreen(new SystemMenuScreen(type));
    }

    public static void open(MenuType type, BlockPos pos) {
        Minecraft.getInstance().setScreen(new SystemMenuScreen(type, pos));
    }

    @Override
    protected void init() {
        GameNetwork.requestStatus();
        int center = width / 2;
        int top = height / 2 - 20;

        switch (type) {
            case TRAINING -> {
                addRenderableWidget(Button.builder(Component.literal("Treinar +1"), button ->
                        GameNetwork.CHANNEL.sendToServer(new GameNetwork.TrainOncePacket(trainingPos)))
                        .bounds(center - 75, top + 30, 150, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> onClose())
                        .bounds(center - 75, top + 58, 150, 20).build());
            }
            case SHOP -> {
                addRenderableWidget(Button.builder(Component.literal("Comida básica — " + EconomyRules.BASIC_FOOD_PRICE),
                        button -> buy(0)).bounds(center - 110, top + 5, 220, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Comida rara — " + EconomyRules.RARE_FOOD_PRICE),
                        button -> buy(1)).bounds(center - 110, top + 31, 220, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Comida premium — " + EconomyRules.PREMIUM_FOOD_PRICE),
                        button -> buy(2)).bounds(center - 110, top + 57, 220, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> onClose())
                        .bounds(center - 75, top + 88, 150, 20).build());
            }
            case SALES -> {
                addRenderableWidget(Button.builder(Component.literal("Vender 1 animal"), button ->
                        GameNetwork.CHANNEL.sendToServer(new GameNetwork.SellAnimalsPacket(false)))
                        .bounds(center - 90, top + 28, 180, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Vender todos"), button ->
                        GameNetwork.CHANNEL.sendToServer(new GameNetwork.SellAnimalsPacket(true)))
                        .bounds(center - 90, top + 54, 180, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> onClose())
                        .bounds(center - 75, top + 84, 150, 20).build());
            }
            case PEN -> addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> onClose())
                    .bounds(center - 75, top + 70, 150, 20).build());
            case PHASES -> buildPhaseButtons(center, top);
        }
        lastPhaseCount = ClientGameData.phaseCount;
    }

    private void buy(int foodId) {
        GameNetwork.CHANNEL.sendToServer(new GameNetwork.PurchaseFoodPacket(foodId, 1));
        GameNetwork.requestStatus();
    }

    private void buildPhaseButtons(int center, int top) {
        int count = ClientGameData.phaseCount;
        if (count <= 0) {
            addRenderableWidget(Button.builder(Component.literal("Configurar quantidade de fases"),
                    button -> PhaseClient.open()).bounds(center - 110, top + 30, 220, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> onClose())
                    .bounds(center - 75, top + 60, 150, 20).build());
            return;
        }

        int pageCount = (count + 7) / 8;
        phasePage = Math.max(0, Math.min(phasePage, pageCount - 1));
        int first = phasePage * 8;
        int last = Math.min(count, first + 8);
        for (int phase = first; phase < last; phase++) {
            final int phaseIndex = phase;
            int column = (phase - first) % 4;
            int row = (phase - first) / 4;
            String label = (ClientGameData.isPhaseDone(phase) ? "✓ " : "") + "Fase " + (phase + 1);
            addRenderableWidget(Button.builder(Component.literal(label), button -> {
                activePhase = phaseIndex;
                clearWidgets();
                init();
            }).bounds(center - 150 + column * 76, top + 8 + row * 25, 72, 20).build());
        }

        if (pageCount > 1) {
            addRenderableWidget(Button.builder(Component.literal("◀"), button -> {
                phasePage = Math.max(0, phasePage - 1);
                clearWidgets();
                init();
            }).bounds(center - 145, top + 68, 35, 20).build());
            addRenderableWidget(Button.builder(Component.literal("▶"), button -> {
                phasePage = Math.min(pageCount - 1, phasePage + 1);
                clearWidgets();
                init();
            }).bounds(center + 110, top + 68, 35, 20).build());
        }

        activePhase = Math.max(0, Math.min(activePhase, count - 1));
        addRenderableWidget(Button.builder(Component.literal(
                ClientGameData.isPhaseDone(activePhase) ? "CONCLUÍDA" : "FEITO"),
                button -> GameNetwork.CHANNEL.sendToServer(
                        new GameNetwork.SetPhaseDonePacket(activePhase)))
                .bounds(center - 75, top + 98, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"), button -> onClose())
                .bounds(center - 75, top + 124, 150, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (type == MenuType.PHASES && lastPhaseCount != ClientGameData.phaseCount) {
            clearWidgets();
            init();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelWidth = type == MenuType.PHASES ? 360 : 330;
        int panelHeight = type == MenuType.PHASES ? 220 : 190;
        int left = width / 2 - panelWidth / 2;
        int top = height / 2 - panelHeight / 2;
        graphics.fill(left, top, left + panelWidth, top + panelHeight, BLUE_PANEL);
        graphics.fill(left, top, left + panelWidth, top + 3, GOLD);
        graphics.fill(left, top + panelHeight - 3, left + panelWidth, top + panelHeight, GOLD);
        graphics.fill(left, top, left + 3, top + panelHeight, GOLD);
        graphics.fill(left + panelWidth - 3, top, left + panelWidth, top + panelHeight, GOLD);
        graphics.drawCenteredString(font, title(type).toUpperCase(), width / 2, top + 13, GOLD);

        switch (type) {
            case TRAINING -> {
                graphics.drawCenteredString(font, "Força de arremesso: " + ClientGameData.throwPower
                        + "/" + com.feedanimals.mod.util.PlayerProgress.MAX_THROW_POWER,
                        width / 2, top + 43, 0xFFFFFFFF);
                graphics.drawCenteredString(font, "Ctrl + botão direito na barra: treino automático",
                        width / 2, top + 66, 0xFFBFD7F0);
                graphics.drawCenteredString(font, "Pressione Shift para interromper",
                        width / 2, top + 79, 0xFFBFD7F0);
            }
            case SHOP -> {
                graphics.drawCenteredString(font, "Saldo: " + ClientGameData.money + " moedas",
                        width / 2, top + 38, 0xFFFFFFFF);
                graphics.renderItem(new ItemStack(ModItems.FEED_BASIC.get()), left + 30, top + 58);
                graphics.renderItem(new ItemStack(ModItems.FEED_RARE.get()), left + 30, top + 84);
                graphics.renderItem(new ItemStack(ModItems.FEED_PREMIUM.get()), left + 30, top + 110);
            }
            case SALES -> {
                graphics.drawCenteredString(font, "Saldo: " + ClientGameData.money + " moedas",
                        width / 2, top + 42, 0xFFFFFFFF);
                graphics.drawCenteredString(font, "Animais na mochila: " + ClientGameData.animalTokens,
                        width / 2, top + 56, 0xFFFFFFFF);
                graphics.drawCenteredString(font, "A área delimitada libera a venda dos animais",
                        width / 2, top + 70, 0xFFBFD7F0);
            }
            case PEN -> {
                graphics.drawCenteredString(font, "Animais no cercado: " + ClientGameData.penAnimals,
                        width / 2, top + 48, 0xFFFFFFFF);
                graphics.drawCenteredString(font, "Use os registros de animais dentro da área",
                        width / 2, top + 66, 0xFFBFD7F0);
                graphics.drawCenteredString(font, "Cada animal gera moedas automaticamente",
                        width / 2, top + 80, 0xFFBFD7F0);
            }
            case PHASES -> {
                if (ClientGameData.phaseCount > 0) {
                    int center = width / 2;
                    int stripTop = height / 2 - 12;
                    int first = phasePage * 8;
                    int last = Math.min(ClientGameData.phaseCount, first + 8);
                    for (int phase = first; phase < last; phase++) {
                        int local = phase - first;
                        int x = center - 150 + (local % 4) * 76;
                        int y = stripTop + 8 + (local / 4) * 25;
                        int color = ClientGameData.isPhaseDone(phase) ? 0xFF43D66B
                                : phaseColor(phase);
                        graphics.fill(x - 2, y - 2, x + 74, y + 22, color);
                        graphics.renderItem(new ItemStack(phaseIcon(phase)), x - 13, y + 2);
                    }
                }
                if (ClientGameData.phaseCount <= 0) {
                    graphics.drawCenteredString(font, "Nenhum projeto de fases nesta dimensão.",
                            width / 2, top + 70, 0xFFFFFFFF);
                    graphics.drawCenteredString(font, "Use o item Delimitador de Fases para configurar.",
                            width / 2, top + 86, 0xFFBFD7F0);
                } else {
                    graphics.drawCenteredString(font, "Fase selecionada: " + (activePhase + 1)
                            + " de " + ClientGameData.phaseCount, width / 2, top + 42, 0xFFFFFFFF);
                    graphics.drawCenteredString(font, "Construa manualmente a estrutura desta divisão.",
                            width / 2, top + 62, 0xFFBFD7F0);
                    graphics.drawCenteredString(font, ClientGameData.isPhaseDone(activePhase)
                            ? "Esta fase já foi concluída." : "Quando terminar, pressione FEITO.",
                            width / 2, top + 77, ClientGameData.isPhaseDone(activePhase)
                                    ? 0xFF75E68A : 0xFFFFFFFF);
                    graphics.renderItem(new ItemStack(phaseIcon(activePhase)), width / 2 - 8, top + 93);
                    graphics.drawCenteredString(font, "Concluídas: " + ClientGameData.completedPhases
                            + "/" + ClientGameData.phaseCount, width / 2, top + 112, 0xFFFFFFFF);
                }
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static int phaseColor(int phase) {
        return switch (phase % 6) {
            case 0 -> 0xFFDA5757;
            case 1 -> 0xFFE0A04A;
            case 2 -> 0xFFD6C84C;
            case 3 -> 0xFF54B56A;
            case 4 -> 0xFF4A9EDB;
            default -> 0xFF9A6BD1;
        };
    }

    private static net.minecraft.world.item.Item phaseIcon(int phase) {
        return switch (phase % 4) {
            case 0 -> Items.OAK_LOG;
            case 1 -> Items.BONE;
            case 2 -> Items.STONE_BRICKS;
            default -> Items.TORCH;
        };
    }

    private static String title(MenuType type) {
        return switch (type) {
            case PEN -> "Gerenciamento do Cercado";
            case SALES -> "Vendas de Animais";
            case TRAINING -> "Treinamento";
            case SHOP -> "Loja de Alimentos";
            case PHASES -> "Gerenciamento de Fases";
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
