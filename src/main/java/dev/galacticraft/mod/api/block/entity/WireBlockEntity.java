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

package dev.galacticraft.mod.api.block.entity;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProviderBlockEntity;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.accessor.WorldRendererAccessor;
import dev.galacticraft.mod.api.wire.Wire;
import dev.galacticraft.mod.api.wire.WireNetwork;
import dev.galacticraft.mod.attribute.energy.WireEnergyInsertable;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class WireBlockEntity extends BlockEntity implements Wire, AttributeProviderBlockEntity, BlockEntityClientSerializable {
    private @Nullable WireNetwork network = null;
    private @NotNull WireEnergyInsertable @Nullable[] insertables = null;
    private final int maxTransferRate;
    private final boolean[] connections = new boolean[6];

    public WireBlockEntity(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state, int maxTransferRate) {
        super(type, pos, state);
        this.maxTransferRate = maxTransferRate;
    }

    public static WireBlockEntity createT1(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state) {
        return new WireBlockEntity(type, pos, state, 240);
    }

    public static WireBlockEntity createT2(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state) {
        return new WireBlockEntity(type, pos, state, 480);
    }

    protected WireBlockEntity(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setNetwork(@Nullable WireNetwork network) {
        this.network = network;
        for (WireEnergyInsertable insertable : this.getInsertables()) {
            insertable.setNetwork(network);
        }
    }

    @Override
    public @NotNull WireNetwork getOrCreateNetwork() {
        if (this.network == null) {
            if (!this.world.isClient()) {
                for (Direction direction : Constant.Misc.DIRECTIONS) {
                    BlockEntity entity = world.getBlockEntity(pos.offset(direction));
                    if (entity instanceof Wire wire && wire.getNetwork() != null) {
                        if (wire.getOrCreateNetwork().canAccept(this, direction.getOpposite())) {
                            wire.getOrCreateNetwork().addWire(pos, this);
                        }
                    }
                }
                if (this.network == null) {
                    this.setNetwork(WireNetwork.create((ServerWorld) world, this.getMaxTransferRate()));
                    this.network.addWire(pos, this);
                }
            }
        }
        return this.network;
    }

    @Override
    public @Nullable WireNetwork getNetwork() {
        return this.network;
    }

    public @NotNull WireEnergyInsertable @NotNull[] getInsertables() {
        if (this.insertables == null) {
            this.insertables = new WireEnergyInsertable[6];
            for (Direction direction : Constant.Misc.DIRECTIONS) {
                this.insertables[direction.ordinal()] = new WireEnergyInsertable(direction, this.getMaxTransferRate(), this.pos);
            }
        }
        return this.insertables;
    }

    @Override
    public int getMaxTransferRate() {
        return maxTransferRate;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (this.getNetwork() != null) {
            this.getOrCreateNetwork().removeWire(this.pos);
        }
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        if (to.getSearchDirection() != null) {
            to.offer(this.getInsertables()[to.getSearchDirection().ordinal()]);
        }
    }

    @Override
    public boolean[] connections() {
        return this.connections;
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        this.readConnections(tag);
        ((WorldRendererAccessor) MinecraftClient.getInstance().worldRenderer).addChunkToRebuild(this.pos);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        this.writeConnections(tag);
        return tag;
    }

    private void writeConnections(NbtCompound tag) {
        tag.putBoolean("0", this.connections[0]);
        tag.putBoolean("1", this.connections[1]);
        tag.putBoolean("2", this.connections[2]);
        tag.putBoolean("3", this.connections[3]);
        tag.putBoolean("4", this.connections[4]);
        tag.putBoolean("5", this.connections[5]);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        this.writeConnections(nbt);
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.readConnections(nbt);
        super.readNbt(nbt);
    }

    private void readConnections(NbtCompound nbt) {
        this.connections[0] = nbt.getBoolean("0");
        this.connections[1] = nbt.getBoolean("1");
        this.connections[2] = nbt.getBoolean("2");
        this.connections[3] = nbt.getBoolean("3");
        this.connections[4] = nbt.getBoolean("4");
        this.connections[5] = nbt.getBoolean("5");
    }
}
