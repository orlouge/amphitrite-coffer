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
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");

    public AmphitriteCofferScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.passEvents = false;
        int i = 222;
        int j = 114;
        this.backgroundHeight = 150;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(0.8f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, 53);
        this.drawTexture(matrices, i, j + 53, 0, 126, this.backgroundWidth, 96);
    }
}
