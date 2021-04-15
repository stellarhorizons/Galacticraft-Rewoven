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

package dev.galacticraft.mod.api.machine;

import alexiil.mc.lib.attributes.misc.Saveable;
import dev.galacticraft.mod.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

public class MachineConfiguration implements Saveable {
    private MachineStatus status = MachineStatus.NULL;
    private RedstoneInteractionType redstone = RedstoneInteractionType.IGNORE;
    private final MachineIOConfig configuration = new MachineIOConfig();
    private final SecurityInfo security = new SecurityInfo();

    public MachineConfiguration() {

    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }

    public void setRedstone(RedstoneInteractionType redstone) {
        this.redstone = redstone;
    }

    public MachineIOConfig getSideConfiguration() {
        return configuration;
    }

    public SecurityInfo getSecurity() {
        return security;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public RedstoneInteractionType getRedstoneInteraction() {
        return redstone;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.put(Constants.Nbt.SECURITY, this.getSecurity().toTag(new CompoundTag()));
        tag.put(Constants.Nbt.CONFIGURATION, this.getSideConfiguration().toTag(new CompoundTag()));
        this.redstone.toTag(tag);
        return tag;
    }

    public CompoundTag toClientTag(CompoundTag tag, PlayerEntity player) {
        if (security.hasAccess(player)) {
            tag.put(Constants.Nbt.SECURITY, this.getSecurity().toTag(new CompoundTag()));
            tag.put(Constants.Nbt.CONFIGURATION, this.getSideConfiguration().toTag(new CompoundTag()));
            this.redstone.toTag(tag);
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.getSecurity().fromTag(tag.getCompound(Constants.Nbt.SECURITY));
        this.getSideConfiguration().fromTag(tag.getCompound(Constants.Nbt.CONFIGURATION));
        this.redstone = RedstoneInteractionType.fromTag(tag);
    }

    public static MachineConfiguration fromClientTag(CompoundTag tag) {
        MachineConfiguration configuration = new MachineConfiguration();
        if (tag.contains(Constants.Nbt.REDSTONE_INTERACTION_TYPE)) {
            configuration.setRedstone(RedstoneInteractionType.fromTag(tag));
        }
        if (tag.contains(Constants.Nbt.CONFIGURATION)) {
            configuration.getSideConfiguration().fromTag(tag.getCompound(Constants.Nbt.CONFIGURATION));
        }
        if (tag.contains(Constants.Nbt.SECURITY)) {
            configuration.getSecurity().fromTag(tag.getCompound(Constants.Nbt.SECURITY));
        }
        return configuration;
    }
}
