/*
 * Copyright (c) 2019-2024 Team Galacticraft
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

package dev.galacticraft.mod.screen;

import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.mod.content.GCMachineTypes;
import dev.galacticraft.mod.content.block.entity.machine.FuelLoaderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Consumer;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class FuelLoaderMenu extends MachineMenu<FuelLoaderBlockEntity> {
    public long rocketAmount;
    public long rocketCapacity;
    public BlockPos connectionPos = BlockPos.ZERO;

    public FuelLoaderMenu(int syncId, ServerPlayer player, FuelLoaderBlockEntity machine) {
        super(syncId, player, machine);
    }

    public FuelLoaderMenu(int syncId, Inventory inv, FriendlyByteBuf buf) {
        super(syncId, inv, buf, 8, 84, GCMachineTypes.FUEL_LOADER);
    }

    @Override
    public void registerSyncHandlers(Consumer<MenuSyncHandler> consumer) {
        super.registerSyncHandlers(consumer);
        consumer.accept(MenuSyncHandler.simple(() -> this.machine.getConnectionPos().asLong(), l -> this.connectionPos = (BlockPos.of(l))));
        consumer.accept(MenuSyncHandler.simple(() -> this.machine.linkedRocket == null ? 0 : this.machine.linkedRocket.getFuelTankAmount(), l -> this.rocketAmount = l));
        consumer.accept(MenuSyncHandler.simple(() -> this.machine.linkedRocket == null ? 0 : this.machine.linkedRocket.getFuelTankCapacity(), l -> this.rocketCapacity = l));
    }
}