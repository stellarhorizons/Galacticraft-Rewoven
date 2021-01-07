/*
 * Copyright (c) 2020 HRZN LTD
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

package com.hrznstudio.galacticraft.client.gui.screen.ingame;

import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.Galacticraft;
import com.hrznstudio.galacticraft.api.screen.MachineHandledScreen;
import com.hrznstudio.galacticraft.client.gui.widget.machine.CapacitorWidget;
import com.hrznstudio.galacticraft.screen.AdvancedSolarPanelScreenHandler;
import com.hrznstudio.galacticraft.util.DrawableUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
@Environment(EnvType.CLIENT)
public class AdvancedSolarPanelScreen extends MachineHandledScreen<AdvancedSolarPanelScreenHandler> {

    private static final Identifier BACKGROUND = new Identifier(Constants.MOD_ID, Constants.ScreenTextures.getRaw(Constants.ScreenTextures.SOLAR_PANEL_SCREEN));

    public AdvancedSolarPanelScreen(AdvancedSolarPanelScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, inv.player.world, handler.blockEntity.getPos(), title);
        this.addWidget(new CapacitorWidget(handler.blockEntity.getCapacitor(), 8, 8, 48, this::getEnergyTooltipLines, handler.blockEntity::getStatus));
    }

    @Override
    protected void drawBackground(MatrixStack stack, float v, int mouseX, int mouseY) {
        this.renderBackground(stack);
        this.client.getTextureManager().bindTexture(BACKGROUND);

        int leftPos = this.x;
        int topPos = this.y;

        this.drawTexture(stack, leftPos, topPos, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float v) {
        super.render(stack, mouseX, mouseY, v);
        DrawableUtils.drawCenteredString(stack, this.client.textRenderer, new TranslatableText("block.galacticraft-rewoven.advanced_solar_panel"), (this.width / 2), this.y + 5, Formatting.DARK_GRAY.getColorValue());
        this.drawMouseoverTooltip(stack, mouseX, mouseY);
    }

    @Override
    @NotNull
    protected Collection<? extends Text> getEnergyTooltipLines() {
        List<Text> lines = new ArrayList<>();
        if (this.handler.blockEntity.getStatus().getType().isActive()) {
            long time = world.getTimeOfDay() % 24000;
            if (time > 6000) {
                time = 6000 - (time - 6000);
            }

            lines.add(new TranslatableText("ui.galacticraft-rewoven.machine.gj_per_t", (int) Math.min(Galacticraft.configManager.get().solarPanelEnergyProductionRate(), (Galacticraft.configManager.get().solarPanelEnergyProductionRate() * (time / 6000D) * this.handler.blockEntity.multiplier) * 4)).setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)));
        }
        return lines;
    }
}
