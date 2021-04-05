/*
 * Copyright (c) 2019-2021 HRZN LTD
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

package com.hrznstudio.galacticraft.api.block;

import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.api.block.entity.MachineBlockEntity;
import com.hrznstudio.galacticraft.attribute.Automatable;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public enum AutomationType implements Comparable<AutomationType> {
    NONE(new TranslatableText("ui.galacticraft-rewoven.side_option.none").setStyle(Constants.Text.DARK_GRAY_STYLE), false, false, false, false, false),
    POWER_INPUT(new TranslatableText("ui.galacticraft-rewoven.side_option.energy").setStyle(Constants.Text.LIGHT_PURPLE_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.in").setStyle(Constants.Text.GREEN_STYLE)), true, false, false, true, false),
    POWER_OUTPUT(new TranslatableText("ui.galacticraft-rewoven.side_option.energy").setStyle(Constants.Text.LIGHT_PURPLE_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.out").setStyle(Constants.Text.DARK_RED_STYLE)), true, false, false, false, true),
    POWER_IO(new TranslatableText("ui.galacticraft-rewoven.side_option.energy").setStyle(Constants.Text.LIGHT_PURPLE_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.io").setStyle(Constants.Text.BLUE_STYLE)), true, false, false, true, true),
    FLUID_INPUT(new TranslatableText("ui.galacticraft-rewoven.side_option.fluids").setStyle(Constants.Text.GREEN_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.in").setStyle(Constants.Text.GREEN_STYLE)), false, true, false, true, false),
    FLUID_OUTPUT(new TranslatableText("ui.galacticraft-rewoven.side_option.fluids").setStyle(Constants.Text.GREEN_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.out").setStyle(Constants.Text.DARK_RED_STYLE)), false, true, false, false, true),
    FLUID_IO(new TranslatableText("ui.galacticraft-rewoven.side_option.fluids").setStyle(Constants.Text.GREEN_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.io").setStyle(Constants.Text.BLUE_STYLE)), false, true, false, true, true),
    ITEM_INPUT(new TranslatableText("ui.galacticraft-rewoven.side_option.items").setStyle(Constants.Text.GOLD_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.in").setStyle(Constants.Text.GREEN_STYLE)), false, false, true, true, false),
    ITEM_OUTPUT(new TranslatableText("ui.galacticraft-rewoven.side_option.items").setStyle(Constants.Text.GOLD_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.out").setStyle(Constants.Text.DARK_RED_STYLE)), false, false, true, false, true),
    ITEM_IO(new TranslatableText("ui.galacticraft-rewoven.side_option.items").setStyle(Constants.Text.GOLD_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.io").setStyle(Constants.Text.BLUE_STYLE)), false, false, true, true, true);
//    ANY(new TranslatableText("ui.galacticraft-rewoven.side_option.any").setStyle(Constants.Text.RED_STYLE).append(new TranslatableText("ui.galacticraft-rewoven.side_option.io").setStyle(Constants.Text.BLUE_STYLE)), true, true, true, true, true);

    private final MutableText name;
    private final boolean energy;
    private final boolean fluid;
    private final boolean item;
    private final boolean input;
    private final boolean output;

    AutomationType(MutableText name, boolean energy, boolean fluid, boolean item, boolean input, boolean output) {
        this.name = name;
        this.energy = energy;
        this.fluid = fluid;
        this.item = item;
        this.input = input;
        this.output = output;
    }

    public boolean isEnergy() {
        return energy;
    }

    public boolean isItem() {
        return item;
    }

    public boolean isFluid() {
        return fluid;
    }

    public boolean isInput() {
        return input;
    }

    public boolean isOutput() {
        return output;
    }

    public boolean canPassAs(AutomationType other) {
        if (other == this) return true;
        if (other.isEnergy()) if (!this.isEnergy()) return false;
        if (other.isFluid()) if (!this.isFluid()) return false;
        if (other.isItem()) if (!this.isItem()) return false;
        if (other.isInput()) if (!this.isInput()) return false;
        if (other.isOutput()) return this.isOutput();
        return true;
    }

    public Automatable getAutomatable(MachineBlockEntity machine) {
        if (this.isItem()) return machine.getInventory();
        if (this.isFluid()) return machine.getFluidTank();
        if (this.isEnergy()) throw new UnsupportedOperationException("NYI");
        return null;
    }

    public Text getFormattedName() {
        return this.name;
    }
}
