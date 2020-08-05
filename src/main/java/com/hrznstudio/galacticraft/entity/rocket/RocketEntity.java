/*
 * Copyright (c) 2019 HRZN LTD
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

package com.hrznstudio.galacticraft.entity.rocket;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.Galacticraft;
import com.hrznstudio.galacticraft.api.rocket.LaunchStage;
import com.hrznstudio.galacticraft.api.rocket.part.RocketPart;
import com.hrznstudio.galacticraft.api.rocket.part.RocketPartType;
import com.hrznstudio.galacticraft.block.GalacticraftBlocks;
import com.hrznstudio.galacticraft.block.special.rocketlaunchpad.RocketLaunchPadBlock;
import com.hrznstudio.galacticraft.block.special.rocketlaunchpad.RocketLaunchPadBlockEntity;
import com.hrznstudio.galacticraft.client.gui.screen.ingame.PlanetSelectScreen;
import com.hrznstudio.galacticraft.screen.BasicSolarPanelScreenHandler;
import com.hrznstudio.galacticraft.tag.GalacticraftTags;
import io.github.cottonmc.component.UniversalComponents;
import io.github.cottonmc.component.api.ActionType;
import io.github.cottonmc.component.fluid.impl.EntitySyncedTankComponent;
import io.github.fablabsmc.fablabs.api.fluidvolume.v1.Fraction;
import io.netty.buffer.Unpooled;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class RocketEntity extends Entity implements EntityComponentCallback<RocketEntity>, ComponentProvider { //pitch+90

    private static final TrackedData<LaunchStage> STAGE = DataTracker.registerData(RocketEntity.class, new TrackedDataHandler<LaunchStage>() {
        @Override
        public void write(PacketByteBuf var1, LaunchStage var2) {
            var1.writeEnumConstant(var2);
        }

        @Override
        public LaunchStage read(PacketByteBuf var1) {
            return var1.readEnumConstant(LaunchStage.class);
        }

        @Override
        public LaunchStage copy(LaunchStage var1) {
            return var1;
        }
    });

    private final EntitySyncedTankComponent tank = new EntitySyncedTankComponent(1, Fraction.ofWhole(12), UniversalComponents.TANK_COMPONENT, this);
    
    private static final TrackedData<Float[]> COLOR = DataTracker.registerData(RocketEntity.class, new TrackedDataHandler<Float[]>() {

    @Override
    public void write(PacketByteBuf var1, Float[] var2) {
        assert var2.length > 3;
        var1.writeFloat(var2[0]);
        var1.writeFloat(var2[1]);
        var1.writeFloat(var2[2]);
        var1.writeFloat(var2[3]);
    }

    @Override
    public Float[] read(PacketByteBuf var1) {
        return new Float[] {var1.readFloat(), var1.readFloat(), var1.readFloat(), var1.readFloat()};
    }

    @Override
    public Float[] copy(Float[] var1) {
        return var1;
    }
});

    public static final TrackedData<Integer> DAMAGE_WOBBLE_TICKS = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> DAMAGE_WOBBLE_SIDE = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Float> DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public static final TrackedData<Double> SPEED = DataTracker.registerData(RocketEntity.class, new TrackedDataHandler<Double>() {
        @Override
        public void write(PacketByteBuf var1, Double var2) {
            var1.writeDouble(var2);
        }

        @Override
        public Double read(PacketByteBuf var1) {
            return var1.readDouble();
        }

        @Override
        public Double copy(Double var1) {
            return var1;
        }
    });

    public static final TrackedData<RocketPart[]> PARTS = DataTracker.registerData(RocketEntity.class, new TrackedDataHandler<RocketPart[]>() {
        @Override
        public void write(PacketByteBuf var1, RocketPart[] var2) {
            for (byte i = 0; i < RocketPartType.values().length; i++) {
                var1.writeBoolean(var2[i] != null);
                if (var2[i] != null) {
                    var1.writeIdentifier(Galacticraft.ROCKET_PARTS.getId(var2[i]));
                }
            }
        }

        @Override
        public RocketPart[] read(PacketByteBuf var1) {
            RocketPart[] array = new RocketPart[RocketPartType.values().length];
            for (byte i = 0; i < RocketPartType.values().length; i++) {
                if (var1.readBoolean()) {
                    array[i] = Galacticraft.ROCKET_PARTS.get(var1.readIdentifier());
                }
            }
            return array;
        }

        @Override
        public RocketPart[] copy(RocketPart[] var1) {
            RocketPart[] parts = new RocketPart[RocketPartType.values().length];
            System.arraycopy(var1, 0, parts, 0, var1.length);
            return parts;
        }
    });
    private final boolean debugMode = false && FabricLoader.getInstance().isDevelopmentEnvironment();

    static {
        TrackedDataHandlerRegistry.register(STAGE.getType());
        TrackedDataHandlerRegistry.register(COLOR.getType());
        TrackedDataHandlerRegistry.register(SPEED.getType());
        TrackedDataHandlerRegistry.register(PARTS.getType());
    }

    private BlockPos linkedPad = new BlockPos(0, 0, 0);

    public RocketEntity(EntityType<RocketEntity> type, World world_1) {
        super(type, world_1);
    }

    public Float[] getColor() {
        return this.dataTracker.get(COLOR);
    }

    @Override
    protected boolean canAddPassenger(Entity entity_1) {
        return this.getPassengerList().isEmpty();
    }

    private long timeAsState = 0;

    public BlockPos getLinkedPad() {
        return linkedPad;
    }

    @Override
    public boolean damage(DamageSource damageSource_1, float float_1) {
        if (!this.world.isClient && !this.removed) {
            if (this.isInvulnerableTo(damageSource_1)) {
                return false;
            } else {
                this.dataTracker.set(DAMAGE_WOBBLE_SIDE, -this.dataTracker.get(DAMAGE_WOBBLE_SIDE));
                this.dataTracker.set(DAMAGE_WOBBLE_TICKS, 10);
                this.dataTracker.set(DAMAGE_WOBBLE_STRENGTH, this.dataTracker.get(DAMAGE_WOBBLE_STRENGTH) + float_1 * 10.0F);
                boolean boolean_1 = damageSource_1.getAttacker() instanceof PlayerEntity && ((PlayerEntity)damageSource_1.getAttacker()).abilities.creativeMode;
                if (boolean_1 || this.dataTracker.get(DAMAGE_WOBBLE_STRENGTH) > 40.0F) {
                    this.removeAllPassengers();
                    if (boolean_1 && !this.hasCustomName()) {
                        this.remove();
                    } else {
                        this.dropItems(damageSource_1, false);
                    }
                }

                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (this.linkedPad != null) {
            if (this.world.getBlockEntity(this.linkedPad) instanceof RocketLaunchPadBlockEntity){
                ((RocketLaunchPadBlockEntity) this.world.getBlockEntity(this.linkedPad)).setRocketEntityId(Integer.MIN_VALUE);
                ((RocketLaunchPadBlockEntity) this.world.getBlockEntity(this.linkedPad)).setRocketEntityUUID(null);
            }

        }
    }

    @Override
    protected boolean canStartRiding(Entity entity_1) {
        return false;
    }

    @Override
    public void updateTrackedPosition(double double_1, double double_2, double double_3) {
        super.updateTrackedPosition(double_1, double_2, double_3);
    }

    @Override
    public boolean canUsePortals() {
        return true;
    }

    @Override
    public ActionResult interactAt(PlayerEntity playerEntity_1, Vec3d vec3d_1, Hand hand_1) {
        playerEntity_1.startRiding(this);
        return ActionResult.SUCCESS;
    }

    public int getTier() {
        int tier = 0;
        for (RocketPart part : this.getParts()) {
            if (part != null) tier = Math.max(part.getTier(Lists.newArrayList(this.getParts())), tier);
        }
        return tier;
    }

    public void setLinkedPad(BlockPos linkedPad) {
        this.linkedPad = linkedPad;
    }

    @Override
    public ActionResult interact(PlayerEntity playerEntity_1, Hand hand_1) {
        if (this.getPassengerList().isEmpty()) {
            playerEntity_1.startRiding(this);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public boolean collides() { //Required to interact with the entity
        return true;
    }

    @Override
    public void updatePassengerPosition(Entity entity_1) {
        if (this.hasPassenger(entity_1)) {
            entity_1.updatePosition(this.getX(), this.getY() + this.getMountedHeightOffset() + entity_1.getHeightOffset() - 2.5, this.getZ());
            entity_1.prevX = this.prevX;
            entity_1.prevY = this.prevY + this.getMountedHeightOffset() + entity_1.getHeightOffset() - 2.5;
            entity_1.prevZ = this.prevZ;
            entity_1.setVelocity(Vec3d.ZERO);
        }
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        this.tank.fromTag(tag);

        CompoundTag parts = tag.getCompound("Parts");
        RocketPart[] list = new RocketPart[RocketPartType.values().length];
        for (RocketPartType type : RocketPartType.values()) {
            if (parts.contains(type.asString())) {
                list[type.ordinal()] = Galacticraft.ROCKET_PARTS.get(new Identifier(parts.getString(type.asString())));
            }
        }

        setParts(list);

        if (tag.contains("Color")) {
            CompoundTag color = tag.getCompound("Color");
            this.setColor(color.getFloat("red"), color.getFloat("green"), color.getFloat("blue"), color.getFloat("alpha"));
        }

        if (tag.contains("Stage")) {
            this.setStage(LaunchStage.valueOf(tag.getString("Stage")));
        }

        if (tag.contains("Speed")) {
            setSpeed(tag.getDouble("Speed"));
        }

        this.linkedPad = new BlockPos(tag.getInt("lX"), tag.getInt("lY"), tag.getInt("lZ"));
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        this.tank.toTag(tag);

        CompoundTag parts = new CompoundTag();
        CompoundTag color = new CompoundTag();

        for (RocketPart part : this.getParts()) {
            if (part != null) {
                part.toTag(parts);
            }
        }

        color.putFloat("red", this.getColor()[0]);
        color.putFloat("green", this.getColor()[1]);
        color.putFloat("blue", this.getColor()[2]);
        color.putFloat("alpha", this.getColor()[3]);

        tag.putString("Stage", getStage().name());
        tag.putDouble("Speed", this.getSpeed());

        tag.put("Color", color);
        tag.put("Parts", parts);

        tag.putInt("lX", linkedPad.getX());
        tag.putInt("lY", linkedPad.getY());
        tag.putInt("lZ", linkedPad.getZ());
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public void setCustomName(@Nullable Text text_1) {

    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Nullable
    @Override
    public Text getCustomName() {
        return null;
    }

    @Override
    public void setCustomNameVisible(boolean boolean_1) {

    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return () -> Collections.singleton(ItemStack.EMPTY).iterator();
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(STAGE, LaunchStage.IDLE);
        dataTracker.startTracking(COLOR, new Float[]{255.0F, 255.0F, 255.0F, 255.0F});
        dataTracker.startTracking(SPEED, 0.0D);

        dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
        dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 0);
        dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0F);

        RocketPart[] parts = new RocketPart[RocketPartType.values().length];
        dataTracker.startTracking(PARTS, parts);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(Registry.ENTITY_TYPE.getRawId(this.getType())).writeVarInt(this.getEntityId())
                .writeUuid(this.uuid).writeDouble(getX()).writeDouble(getY()).writeDouble(getZ()).writeByte((int) (pitch / 360F * 256F)).writeByte((int) (yaw / 360F * 256F));

        CompoundTag tag = new CompoundTag();
        tag.putFloat("tier", getTier());
        tag.putFloat("red", getColor()[0]);
        tag.putFloat("green", getColor()[1]);
        tag.putFloat("blue", getColor()[2]);
        tag.putFloat("alpha", getColor()[3]);
        RocketPart part = this.getPartForType(RocketPartType.CONE);
        if (part != null) tag.putString("cone", Galacticraft.ROCKET_PARTS.getId(part).toString());

        part = this.getPartForType(RocketPartType.BODY);
        if (part != null) tag.putString("body", Galacticraft.ROCKET_PARTS.getId(part).toString());

        part = this.getPartForType(RocketPartType.FIN);
        if (part != null) tag.putString("fin", Galacticraft.ROCKET_PARTS.getId(part).toString());

        part = this.getPartForType(RocketPartType.BOOSTER);
        if (part != null) tag.putString("booster", Galacticraft.ROCKET_PARTS.getId(part).toString());

        part = this.getPartForType(RocketPartType.BOTTOM);
        if (part != null) tag.putString("bottom", Galacticraft.ROCKET_PARTS.getId(part).toString());

        part = this.getPartForType(RocketPartType.UPGRADE);
        if (part != null) tag.putString("upgrade", Galacticraft.ROCKET_PARTS.getId(part).toString());

        buf.writeCompoundTag(tag);

        return new CustomPayloadS2CPacket(new Identifier(Constants.MOD_ID, "rocket_spawn"),
                new PacketByteBuf(buf));

    }

    @Override
    public void tick() {
        this.noClip = false;
        timeAsState++;

        super.tick();

        if (!world.isClient && world instanceof ServerWorld) {
            if (this.getPassengerList().isEmpty()) {
                if (getStage() != LaunchStage.FAILED) {
                    if (getStage().ordinal() >= LaunchStage.LAUNCHED.ordinal()) {
                        this.setStage(LaunchStage.FAILED);
                    } else {
                        this.setStage(LaunchStage.IDLE);
                    }
                }
            } else if (!(this.getPassengerList().get(0) instanceof PlayerEntity) && this.getStage() != LaunchStage.FAILED) {
                if (getStage() == LaunchStage.LAUNCHED) {
                    this.setStage(LaunchStage.FAILED);
                } else {
                    this.setStage(LaunchStage.IDLE);
                }

                this.removePassenger(this.getPassengerList().get(0));
            }

            if (isOnFire() && !world.isClient) {
                world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
                world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
                world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
                world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
                this.remove();
            }

            if (getStage() == LaunchStage.IGNITED) {
                if (this.tank.getContents(0).isEmpty() && !debugMode) {
                    this.setStage(LaunchStage.IDLE);
                    if (this.getPassengerList().get(0) instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) this.getPassengerList().get(0)).sendMessage(new TranslatableText("chat.galacticraft-rewoven.rocket.no_fuel"), false);
                    }
                    return;
                }
                this.tank.takeFluid(0, Fraction.of(1, 100), ActionType.PERFORM); //todo find balanced values
                if (timeAsState >= 400) {
                    this.setStage(LaunchStage.LAUNCHED);
                    if (!(new BlockPos(0, 0, 0)).equals(this.getLinkedPad())) {
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                if (world.getBlockState(getLinkedPad().add(x, 0, z)).getBlock() == GalacticraftBlocks.ROCKET_LAUNCH_PAD
                                        && world.getBlockState(getLinkedPad().add(x, 0, z)).get(RocketLaunchPadBlock.PART) != RocketLaunchPadBlock.Part.NONE) {
                                    world.setBlockState(getLinkedPad().add(x, 0, z), Blocks.AIR.getDefaultState(), 4);
                                }
                            }
                        }
                    }
                    this.setSpeed(0.0D);
                }
            } else if (getStage() == LaunchStage.LAUNCHED) {
                if (!debugMode && (this.tank.isEmpty() || !this.tank.getContents(0).getFluid().isIn(GalacticraftTags.FUEL))) {
                    this.setStage(LaunchStage.FAILED);
                } else {
                    this.tank.takeFluid(0, Fraction.of(1, 100), ActionType.PERFORM); //todo find balanced values
                    ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5), this.getY(), this.getZ() + (world.random.nextDouble() - 0.5), 0, (world.random.nextDouble() - 0.5), -1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5), this.getY(), this.getZ() + (world.random.nextDouble() - 0.5), 0, (world.random.nextDouble() - 0.5), -1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5), this.getY(), this.getZ() + (world.random.nextDouble() - 0.5), 0, (world.random.nextDouble() - 0.5), -1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5), this.getY(), this.getZ() + (world.random.nextDouble() - 0.5), 0, (world.random.nextDouble() - 0.5), -1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.CLOUD, this.getX() + (world.random.nextDouble() - 0.5), this.getY(), this.getZ() + (world.random.nextDouble() - 0.5), 0, (world.random.nextDouble() - 0.5), -1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.CLOUD, this.getX() + (world.random.nextDouble() - 0.5), this.getY(), this.getZ() + (world.random.nextDouble() - 0.5), 0, (world.random.nextDouble() - 0.5), -1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.CLOUD, this.getX() + (world.random.nextDouble() - 0.5), this.getY(), this.getZ() + (world.random.nextDouble() - 0.5), 0, (world.random.nextDouble() - 0.5), -1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.CLOUD, this.getX() + (world.random.nextDouble() - 0.5), this.getY(), this.getZ() + (world.random.nextDouble() - 0.5), 0, (world.random.nextDouble() - 0.5), -1, world.random.nextDouble() - 0.5, 0.12000000596046448D);


                    this.setSpeed(Math.min(0.75, this.getSpeed() + 0.05D));

                    // Pitch: -45.0
                    // Yaw: 0.0
                    //
                    // X vel: 0.0
                    // Y vel: 0.3535533845424652
                    // Z vel: 0.223445739030838
                    // = 1.58227848
                    //
                    // I hope this is right

                    double velX = -MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos((pitch + 90.0F) / 180.0F * (float) Math.PI) * (this.getSpeed() * 0.632D) * 1.58227848D;
                    double velY = MathHelper.sin((pitch + 90.0F) / 180.0F * (float) Math.PI) * this.getSpeed();
                    double velZ = MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos((pitch + 90.0F) / 180.0F * (float) Math.PI) * (this.getSpeed() * 0.632D) * 1.58227848D;

                    this.setVelocity(velX, velY, velZ);
                }

                if (this.getPos().getY() >= 1200.0F) {
                    for (Entity entity : getPassengerList()) {
                        if (entity instanceof ServerPlayerEntity) {
                            MinecraftClient.getInstance().openScreen(new PlanetSelectScreen());
                            break;
                        }
                    }
                }
            } else if (!onGround) {
                this.setSpeed(Math.max(-1.5F, this.getSpeed() - 0.05D));

                double velX = -MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos((pitch + 90.0F) / 180.0F * (float) Math.PI) * (this.getSpeed() * 0.632D) * 1.58227848D;
                double velY = MathHelper.sin((pitch + 90.0F) / 180.0F * (float) Math.PI) * this.getSpeed();
                double velZ = MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos((pitch + 90.0F) / 180.0F * (float) Math.PI) * (this.getSpeed() * 0.632D) * 1.58227848D;

                this.setVelocity(velX, velY, velZ);
            }

            this.move(MovementType.SELF, this.getVelocity());

            if (getStage() == LaunchStage.FAILED) {
                setRotation((yaw + world.random.nextFloat() - 0.5F * 8.0F) % 360.0F, (pitch + world.random.nextFloat() - 0.5F * 8.0F) % 360.0F);

                ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5) * 0.12F, this.getY() + 2, this.getZ() + (world.random.nextDouble() - 0.5), 0, world.random.nextDouble() - 0.5, 1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5) * 0.12F, this.getY() + 2, this.getZ() + (world.random.nextDouble() - 0.5), 0, world.random.nextDouble() - 0.5, 1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5) * 0.12F, this.getY() + 2, this.getZ() + (world.random.nextDouble() - 0.5), 0, world.random.nextDouble() - 0.5, 1, world.random.nextDouble() - 0.5, 0.12000000596046448D);
                ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, this.getX() + (world.random.nextDouble() - 0.5) * 0.12F, this.getY() + 2, this.getZ() + (world.random.nextDouble() - 0.5), 0, world.random.nextDouble() - 0.5, 1, world.random.nextDouble() - 0.5, 0.12000000596046448D);


                if (this.onGround) {
                    world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
                    world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
                    world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
                    world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
                    this.remove();
                }
            }

            ticksSinceJump++;

        }
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        this.setVelocity(new Vec3d(x, y, z));
    }

    @Override
    public void setVelocity(Vec3d vec3d_1) {
        super.setVelocity(vec3d_1);
        this.velocityDirty = true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean shouldRender(double distance) {
        double d = this.getBoundingBox().getAverageSideLength();
        if (Double.isNaN(d)) {
            d = 1.0D;
        }

        d *= 64.0D * 3;
        return distance < d * d;
    }

    public double getSpeed() {
        return dataTracker.get(SPEED);
    }

    public void setSpeed(double speed) {
        this.dataTracker.set(SPEED, speed);
    }

    @Override
    protected void setRotation(float float_1, float float_2) {
        super.setRotation(float_1, float_2);
        this.getPassengerList().forEach(this::updatePassengerPosition);
    }

    @Override
    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        super.refreshPositionAndAngles(x, y, z, yaw, pitch);
    }

    @Override
    protected void refreshPosition() {
        super.refreshPosition();
        this.getPassengerList().forEach(this::updatePassengerPosition);
    }

    @Override
    public boolean handleFallDamage(float float_1, float float_2) {
        if (this.hasPassengers()) {
            for (Entity entity : this.getPassengerList()) {
                entity.handleFallDamage(fallDistance, float_2);
            }
        }
        return true;
    }

    @Override
    protected void setOnFireFromLava() {
        super.setOnFireFromLava();
        world.createExplosion(this, this.getPos().x + ((world.random.nextDouble() - 0.5) * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + ((world.random.nextDouble() -0.5) * 4), 10.0F, Explosion.DestructionType.DESTROY);
        world.createExplosion(this, this.getPos().x + ((world.random.nextDouble() - 0.5) * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + ((world.random.nextDouble() -0.5) * 4), 10.0F, Explosion.DestructionType.DESTROY);
        world.createExplosion(this, this.getPos().x + ((world.random.nextDouble() - 0.5) * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + ((world.random.nextDouble() -0.5) * 4), 10.0F, Explosion.DestructionType.DESTROY);
        world.createExplosion(this, this.getPos().x + ((world.random.nextDouble() - 0.5) * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + ((world.random.nextDouble() -0.5) * 4), 10.0F, Explosion.DestructionType.DESTROY);
        this.remove();
    }

    @Override
    public boolean doesRenderOnFire() {
        return this.isOnFire();
    }

    @Override
    public void setOnFireFor(int int_1) {
        super.setOnFireFor(int_1);
        if (!world.isClient) {
            world.createExplosion(this, this.getPos().x + ((world.random.nextDouble() - 0.5) * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + ((world.random.nextDouble() - 0.5) * 4), 10.0F, Explosion.DestructionType.DESTROY);
            world.createExplosion(this, this.getPos().x + ((world.random.nextDouble() - 0.5) * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + ((world.random.nextDouble() - 0.5) * 4), 10.0F, Explosion.DestructionType.DESTROY);
            world.createExplosion(this, this.getPos().x + ((world.random.nextDouble() - 0.5) * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + ((world.random.nextDouble() - 0.5) * 4), 10.0F, Explosion.DestructionType.DESTROY);
            world.createExplosion(this, this.getPos().x + ((world.random.nextDouble() - 0.5) * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + ((world.random.nextDouble() - 0.5) * 4), 10.0F, Explosion.DestructionType.DESTROY);
        }
        this.remove();
    }

    @Override
    protected void onBlockCollision(BlockState blockState_1) {
        if (getStage().ordinal() >= LaunchStage.LAUNCHED.ordinal() && timeAsState >= 30 && !(blockState_1.getBlock() instanceof AirBlock) && !world.isClient) {
            world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
            world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
            world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
            world.createExplosion(this, this.getPos().x + (world.random.nextDouble() - 0.5 * 4), this.getPos().y + (world.random.nextDouble() * 3), this.getPos().z + (world.random.nextDouble() - 0.5 * 4), 10.0F, Explosion.DestructionType.DESTROY);
            this.remove();
        }
    }


    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        this.getPassengerList().forEach(this::updatePassengerPosition);
    }

    @Override
    public void move(MovementType movementType_1, Vec3d vec3d_1) {
        if (onGround) vec3d_1.multiply(1.0D, 0.0D, 1.0D);
        super.move(movementType_1, vec3d_1);
        this.getPassengerList().forEach(this::updatePassengerPosition);
    }

    @Override
    protected void removePassenger(Entity entity_1) {
        if (this.getPassengerList().get(0) == entity_1) {
            if (getStage().ordinal() > LaunchStage.IGNITED.ordinal()) {
                this.setStage(LaunchStage.FAILED);
            } else {
                this.setStage(LaunchStage.IDLE);
            }
        }
        super.removePassenger(entity_1);
    }

    private long ticksSinceJump = 0;

    public void onJump() {
        for (RocketPart part : this.getParts()) {
            if (part == null) return;
        }

        if (!this.getPassengerList().isEmpty() && ticksSinceJump > 10) {
            if (this.getPassengerList().get(0) instanceof ServerPlayerEntity) {
                if (getStage().ordinal() < LaunchStage.IGNITED.ordinal()) {
                    if (!tank.getContents(0).isEmpty()) {
                        this.setStage(this.getStage().next());
                        if (getStage() == LaunchStage.WARNING) {
                            ((ServerPlayerEntity) this.getPassengerList().get(0)).sendMessage(new TranslatableText("chat.galacticraft-rewoven.rocket.warning"), true);
                        }
                    }
                }
            }
        }
    }

    public RocketPart[] getParts() {
        return this.dataTracker.get(PARTS);
    }

    public RocketPart getPartForType(RocketPartType type) {
        for (RocketPart part : this.dataTracker.get(PARTS)) {
            if (part != null) {
                if (part.getType() == type) {
                    return part;
                }
            }
        }
        return null;
    }

    public void setPart(RocketPart part) {
        this.dataTracker.get(PARTS)[part.getType().ordinal()] = part;
    }

    public void setParts(RocketPart[] parts) {
        this.dataTracker.set(PARTS, parts);
    }

    public LaunchStage getStage() {
        return this.dataTracker.get(STAGE);
    }

    public void setStage(LaunchStage stage) {
        if (dataTracker.get(STAGE) != stage) {
            this.dataTracker.set(STAGE, stage);
            timeAsState = 0;
        }
    }
    
    public void setColor(float red, float green, float blue, float alpha) {
        this.dataTracker.set(COLOR, new Float[] {red, green, blue, alpha});
    }

    public void dropItems(DamageSource source, boolean exploded) {

    }

    @Override
    public void initComponents(RocketEntity rocketEntity, ComponentContainer<Component> componentContainer) {
        componentContainer.put(UniversalComponents.TANK_COMPONENT, rocketEntity.tank);
    }

    @Override
    public boolean hasComponent(ComponentType<?> componentType) {
        return componentType == UniversalComponents.TANK_COMPONENT;
    }

    @Nullable
    @Override
    public <C extends Component> C getComponent(ComponentType<C> componentType) {
        //noinspection unchecked
        return componentType == UniversalComponents.TANK_COMPONENT ? (C)tank : null;
    }

    @Override
    public @NotNull Set<ComponentType<?>> getComponentTypes() {
        return Sets.newHashSet(UniversalComponents.TANK_COMPONENT);
    }

    public EntitySyncedTankComponent getFuelTank() {
        return tank;
    }

    public void onBaseDestroyed() {
        //todo
    }
}