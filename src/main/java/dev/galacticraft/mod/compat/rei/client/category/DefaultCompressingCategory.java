/*
 * Copyright (c) 2019-2021 Team Galacticraft
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

package dev.galacticraft.mod.compat.rei.client.category;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.block.GalacticraftBlock;
import dev.galacticraft.mod.compat.rei.client.GalacticraftREIClientPlugin;
import dev.galacticraft.mod.compat.rei.client.display.DefaultCompressingDisplay;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
@Environment(EnvType.CLIENT)
public class DefaultCompressingCategory implements DisplayCategory<DefaultCompressingDisplay> {

    @Override
    public CategoryIdentifier<? extends DefaultCompressingDisplay> getCategoryIdentifier() {
        return GalacticraftREIClientPlugin.COMPRESSING;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(new ItemStack(GalacticraftBlock.COMPRESSOR));
    }

    @Override
    public Text getTitle() {
        return new TranslatableText("category.rei.compressing");
    }

    public @NotNull List<Widget> setupDisplay(DefaultCompressingDisplay recipeDisplay, Rectangle bounds) {
        final Point startPoint = new Point(bounds.getCenterX() - 68, bounds.getCenterY() - 37);

        class BaseWidget extends Widget {
            BaseWidget() {
            }

            public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                DiffuseLighting.disableGuiDepthLighting();
                RenderSystem.setShaderTexture(0, Constant.ScreenTexture.REI_DISPLAY_TEXTURE);
                this.drawTexture(matrices, startPoint.x, startPoint.y, 0, 83, 137, 157);

                int height = MathHelper.ceil((double) (System.currentTimeMillis() / 250L) % 14.0D);
                this.drawTexture(matrices, startPoint.x + 2, startPoint.y + 21 + (14 - height), 82, 77 + (14 - height), 14, height);
                int width = MathHelper.ceil((double) (System.currentTimeMillis() / 250L) % 24.0D);
                this.drawTexture(matrices, startPoint.x + 24, startPoint.y + 18, 82, 91, width, 17);
            }

            @Override
            public List<? extends Element> children() {
                return Collections.emptyList();
            }
        }

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(new BaseWidget());
        List<EntryIngredient> input = recipeDisplay.getInputEntries();
        List<Slot> slots = Lists.newArrayList();

        // 3x3 grid
        // Output
        int i;
        for (i = 0; i < 3; ++i) {
            for (int x = 0; x < 3; ++x) {
                slots.add(Widgets.createSlot(new Point(startPoint.x + (x * 18) + 1, startPoint.y + (i * 18) + 1)).markInput());
            }
        }
        for (i = 0; i < input.size(); ++i) {
            if (!input.get(i).isEmpty()) {
                slots.get(this.getSlotWithSize(recipeDisplay, i)).entries(input.get(i));
            }
        }

        widgets.addAll(slots);
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 120, startPoint.y + (18) + 3)).markOutput().entries(recipeDisplay.getOutputEntries().get(0)));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + (2 * 18) + 1, startPoint.y + (18 * 3) + 4)).markInput().entries(AbstractFurnaceBlockEntity.createFuelTimeMap().keySet().stream().map(EntryStacks::of).collect(Collectors.toList())));
        return widgets;
    }

    private int getSlotWithSize(DefaultCompressingDisplay recipeDisplay, int num) {
        if (recipeDisplay.getWidth() == 1) {
            if (num == 1) {
                return 3;
            }

            if (num == 2) {
                return 6;
            }
        }

        if (recipeDisplay.getWidth() == 2) {
            if (num == 2) {
                return 3;
            }

            if (num == 3) {
                return 4;
            }

            if (num == 4) {
                return 6;
            }

            if (num == 5) {
                return 7;
            }
        }

        return num;
    }

    @Override
    public int getMaximumDisplaysPerPage() {
        return 99;
    }

    @Override
    public int getDisplayHeight() {
        return 84;
    }

    @Override
    public int getDisplayWidth(DefaultCompressingDisplay display) {
        return 146;
    }

}