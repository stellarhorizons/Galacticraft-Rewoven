/*
 * Copyright (c) 2019-2022 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.client.gui.screen.ingame;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.galacticraft.api.client.screen.MachineHandledScreen;
import dev.galacticraft.api.screen.SimpleMachineScreenHandler;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.block.entity.OxygenStorageModuleBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
@Environment(EnvType.CLIENT)
public class OxygenStorageModuleScreen extends MachineHandledScreen<OxygenStorageModuleBlockEntity, SimpleMachineScreenHandler<OxygenStorageModuleBlockEntity>> {
    public OxygenStorageModuleScreen(SimpleMachineScreenHandler<OxygenStorageModuleBlockEntity> handler, PlayerInventory inv, Text title) {
        super(handler, inv, title, Constant.ScreenTexture.OXYGEN_STORAGE_MODULE_SCREEN);
    }

    @Override
    protected void renderBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices, mouseX, mouseY, delta);
        this.drawOxygenBufferBar(matrices);

        drawCenteredText(matrices, textRenderer, I18n.translate("ui.galacticraft.machine.current_oxygen", this.machine.gasStorage().getAmount(0)), width / 2, y + 33, Formatting.DARK_GRAY.getColorValue());
        drawCenteredText(matrices, textRenderer, I18n.translate("ui.galacticraft.machine.max_oxygen", this.machine.gasStorage().getMaxCount(0)), width / 2, y + 45, Formatting.DARK_GRAY.getColorValue());
    }

    @Override
    protected void drawTanks(MatrixStack matrices, int mouseX, int mouseY, float delta) {
//        super.drawTanks(matrices, mouseX, mouseY, delta);
    }

    private void drawOxygenBufferBar(MatrixStack matrices) {
        double oxygenScale = (double)this.machine.fluidStorage().getAmount(0) / (double)this.machine.fluidStorage().getMaxCount(0);

        RenderSystem.setShaderTexture(0, Constant.ScreenTexture.OXYGEN_STORAGE_MODULE_SCREEN);
        this.drawTexture(matrices, this.x + 52, this.y + 57, 176, 0, (int) (72.0D * oxygenScale), 3);
    }
}
