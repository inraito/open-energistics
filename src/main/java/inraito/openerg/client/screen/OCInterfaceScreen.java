package inraito.openerg.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import inraito.openerg.Lib;
import inraito.openerg.common.container.OCInterfaceContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class OCInterfaceScreen extends ContainerScreen<OCInterfaceContainer> {
    private final ResourceLocation TEXTURE_RESOURCE = new ResourceLocation(Lib.modid, "textures/gui/container.png");
    private static final int textureWidth = 174;
    private static final int textureHeight = 220;
    public OCInterfaceScreen(OCInterfaceContainer pMenu, PlayerInventory pPlayerInventory, ITextComponent pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.titleLabelX = 8;
        this.titleLabelY = -18;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 100;
    }

    @Override
    protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
        this.renderBackground(pMatrixStack);
        this.minecraft.getTextureManager().bind(TEXTURE_RESOURCE);
        int i = (this.width - textureWidth) / 2;
        int j = (this.height - textureHeight) / 2;
        blit(pMatrixStack, i, j, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        renderBackground(pMatrixStack);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        renderTooltip(pMatrixStack,pMouseX,pMouseY);
    }
}
