package inraito.openerg.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import inraito.openerg.common.container.OCInterfaceContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class OCInterfaceScreen extends ContainerScreen<OCInterfaceContainer> {
    public OCInterfaceScreen(OCInterfaceContainer pMenu, PlayerInventory pPlayerInventory, ITextComponent pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {

    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

    }
}
