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

import com.hrznstudio.galacticraft.api.internal.data.ClientWorldTeamsGetter;
import com.hrznstudio.galacticraft.api.internal.data.MinecraftServerTeamsGetter;
import com.hrznstudio.galacticraft.api.teams.Teams;
import com.hrznstudio.galacticraft.api.teams.data.Team;
import com.mojang.authlib.GameProfile;
import dev.galacticraft.mod.Constants;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SecurityInfo {
    private @Nullable GameProfile owner;
    private @Nullable Identifier team;
    private @NotNull Accessibility accessibility;

    public SecurityInfo() {
        this.accessibility = Accessibility.PUBLIC;
        this.team = null;
    }

    @Contract(pure = true)
    public boolean isOwner(@NotNull PlayerEntity player) {
        return isOwner(player.getGameProfile());
    }

    @Contract(pure = true)
    public boolean isOwner(GameProfile profile) {
        if (this.owner == null) return false;
        return this.owner.equals(profile);
    }

    @Contract(pure = true)
    public boolean hasAccess(PlayerEntity player) {
        if (accessibility == Accessibility.PUBLIC) {
            return true;
        } else if (accessibility == Accessibility.TEAM) {
            if (this.isOwner(player)) return true;
            Team team;
            if (!player.world.isClient()) {
                team = ((MinecraftServerTeamsGetter) player.world.getServer()).getSpaceRaceTeams().getTeam(this.owner.getId());
            } else {
                team = ((ClientWorldTeamsGetter) player.world).getSpaceRaceTeams().getTeam(this.owner.getId());
            }
            if (team == null) return false;
            return team.players.containsKey(player.getUuid());
        } else if (accessibility == Accessibility.PRIVATE) {
            return this.isOwner(player);
        }
        return false;
    }

    public @NotNull Accessibility getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(@NotNull Accessibility accessibility) {
        this.accessibility = accessibility;
    }

    public @Nullable GameProfile getOwner() {
        return this.owner;
    }

    public void setOwner(@NotNull Teams teams, @NotNull PlayerEntity owner) {
        this.setOwner(teams, owner.getGameProfile());
    }

    public void setOwner(@NotNull Teams teams, @NotNull GameProfile owner) {
        if (this.getOwner() == null) {
            this.owner = owner;
            if (teams.getTeam(owner.getId()) != null) this.team = teams.getTeam(owner.getId()).id;
        }
    }

    public @Nullable Identifier getTeam() {
        return team;
    }

    public CompoundTag toTag(CompoundTag tag) {
        if (this.getOwner() != null) {
            tag.put(Constants.Nbt.OWNER, NbtHelper.fromGameProfile(new CompoundTag(), this.getOwner()));
        }
        tag.putString(Constants.Nbt.ACCESSIBILITY, this.accessibility.name());
        if (this.getTeam() != null) {
            tag.putString(Constants.Nbt.TEAM, team.toString());
        }
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        if (tag.contains(Constants.Nbt.OWNER)) {
            this.owner = NbtHelper.toGameProfile(tag.getCompound(Constants.Nbt.OWNER));
        }

        if (tag.contains(Constants.Nbt.TEAM)) {
            this.team = new Identifier(tag.getString(Constants.Nbt.TEAM));
        }

        this.accessibility = Accessibility.valueOf(tag.getString(Constants.Nbt.ACCESSIBILITY));
    }

    public void sendPacket(BlockPos pos, ServerPlayerEntity player) {
        assert this.owner != null;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeByte(this.accessibility.ordinal());
        buf.writeCompoundTag(NbtHelper.fromGameProfile(new CompoundTag(), this.owner));
        ServerPlayNetworking.send(player, new Identifier(Constants.MOD_ID, "security_update"), buf);
    }

    public enum Accessibility implements StringIdentifiable {
        PUBLIC(new TranslatableText("ui.galacticraft.machine.security.accessibility.public")),
        TEAM(new TranslatableText("ui.galacticraft.machine.security.accessibility.team")),
        PRIVATE(new TranslatableText("ui.galacticraft.machine.security.accessibility.private"));

        private final TranslatableText name;

        Accessibility(TranslatableText name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.toString();
        }

        public Text getName() {
            return this.name;
        }
    }
}
