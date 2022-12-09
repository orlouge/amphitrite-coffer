package io.github.orlouge.amphitritecoffer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AmphitriteCofferScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("amphitritecoffer", "textures/gui/container/amphitrite_coffer.png");
    private final AmphitriteCofferScreenHandler screenHandler;

    public AmphitriteCofferScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.passEvents = false;
        this.backgroundHeight = 172;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.screenHandler = (AmphitriteCofferScreenHandler) handler;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(0.8f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        int charge = this.screenHandler.getCharge();
        int maxChargePerColumn = AmphitriteCofferMod.CONFIG.chargePerHeart / 6;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        drawbubbles:
        for (int sidex = 19; sidex <= 110; sidex += 91) {
            for (int bubblex = 0; bubblex < 54; bubblex += 18) {
                if (charge <= 0) break drawbubbles;
                int bubbleCharge = Math.min(charge, maxChargePerColumn);
                charge -= maxChargePerColumn;

                int bubbleHeight = 27 * bubbleCharge / maxChargePerColumn;
                this.drawTexture(matrices, i + sidex + bubblex, j + 6 + 27 - bubbleHeight, 176, 27 -bubbleHeight, 10, bubbleHeight);
            }
        }
    }
}
