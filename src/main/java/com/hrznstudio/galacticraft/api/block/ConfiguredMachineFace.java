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

import com.hrznstudio.galacticraft.api.block.entity.MachineBlockEntity;
import com.hrznstudio.galacticraft.attribute.Automatable;
import com.hrznstudio.galacticraft.screen.slot.SlotType;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class ConfiguredMachineFace {
    private static final Set<AutomationType> CACHED_AUTOMATION_TYPE_SET = new HashSet<>();
    private AutomationType automationType;
    private @Nullable Either<Integer, SlotType> matching;

    public ConfiguredMachineFace(@NotNull AutomationType automationType) {
        this.automationType = automationType;
        this.matching = null;
    }

    public void setOption(@NotNull AutomationType option) {
        this.automationType = option;
        this.matching = null;
    }

    public static List<AutomationType> getValidTypes(MachineBlockEntity machine) {
        List<AutomationType> list = new ArrayList<>();
        CACHED_AUTOMATION_TYPE_SET.clear();
        CACHED_AUTOMATION_TYPE_SET.add(AutomationType.NONE);
        list.add(AutomationType.NONE);

        if (machine.canInsertEnergy()) if (CACHED_AUTOMATION_TYPE_SET.add(AutomationType.POWER_INPUT)) list.add(AutomationType.POWER_INPUT);
        if (machine.canExtractEnergy()) if (CACHED_AUTOMATION_TYPE_SET.add(AutomationType.POWER_OUTPUT)) list.add(AutomationType.POWER_OUTPUT);

        for (SlotType type : machine.getFluidTank().getTypes()) {
            if (type.getType().isBidirectional()) {
                if (CACHED_AUTOMATION_TYPE_SET.add(AutomationType.FLUID_INPUT)) list.add(AutomationType.FLUID_INPUT);
                if (CACHED_AUTOMATION_TYPE_SET.add(AutomationType.FLUID_OUTPUT)) list.add(AutomationType.FLUID_OUTPUT);
            } else {
                if (CACHED_AUTOMATION_TYPE_SET.add(type.getType())) list.add(type.getType());
            }
        }

        for (SlotType type : machine.getInventory().getTypes()) {
            if (type.getType().isBidirectional()) {
                if (CACHED_AUTOMATION_TYPE_SET.add(AutomationType.ITEM_INPUT)) list.add(AutomationType.ITEM_INPUT);
                if (CACHED_AUTOMATION_TYPE_SET.add(AutomationType.ITEM_OUTPUT)) list.add(AutomationType.ITEM_OUTPUT);
            } else {
                if (CACHED_AUTOMATION_TYPE_SET.add(type.getType())) list.add(type.getType());
            }
        }
        return list;
    }

    public void setMatching(@Nullable Either<Integer, SlotType> matching) {
        this.matching = matching;
    }

    public @NotNull AutomationType getAutomationType() {
        return automationType;
    }

    public @Nullable Either<Integer, SlotType> getMatching() {
        return matching;
    }

    public int[] getMatching(Automatable automatable) {
        if (matching != null) {
            if (matching.left().isPresent()) {
                return new int[]{matching.left().get()};
            }
            SlotType type = matching.right().orElseThrow(RuntimeException::new);
            if (type == null) {
                return IntStream.range(0, automatable.getTypes().size() - 1).toArray();
            }
            if (type.getType() == AutomationType.NONE) return new int[0];

            IntList intList = new IntArrayList(1);
            for (int i = 0; i < automatable.getTypes().size(); i++) {
                if (automatable.getTypes().get(i) == type) intList.add(i);
            }
            return intList.toIntArray();
        }
        IntList intList = new IntArrayList(1);
        for (int i = 0; i < automatable.getTypes().size(); i++) {
            if (automatable.getTypes().get(i).getType().canPassAs(this.automationType)) intList.add(i);
        }
        return intList.toIntArray();
    }

    public CompoundTag toTag(CompoundTag tag) {
        tag.putString("option", automationType.name());
        tag.putBoolean("match", this.matching != null);
        if (this.matching != null) {
            tag.putBoolean("left", this.matching.left().isPresent());
            if (this.matching.left().isPresent()) {
                tag.putInt("value", this.matching.left().get());
            } else {
                tag.putString("value", this.matching.right().orElseThrow(RuntimeException::new).getId().toString());
            }
        }
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        this.automationType = AutomationType.valueOf(tag.getString("option"));
        if (tag.getBoolean("match")) {
            if (tag.getBoolean("left")) {
                this.matching = Either.left(tag.getInt("value"));
            } else {
                this.matching = Either.right(SlotType.SLOT_TYPES.get(new Identifier(tag.getString("value"))));
            }
        } else {
            this.matching = null;
        }
    }

}
