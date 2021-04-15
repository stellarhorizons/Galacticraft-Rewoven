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

package dev.galacticraft.mod.client.model;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import dev.galacticraft.mod.Constants;
import dev.galacticraft.mod.Galacticraft;
import dev.galacticraft.mod.api.block.AutomationType;
import dev.galacticraft.mod.api.block.MachineBlock;
import dev.galacticraft.mod.api.block.entity.MachineBlockEntity;
import dev.galacticraft.mod.api.block.util.BlockFace;
import dev.galacticraft.mod.api.machine.MachineConfiguration;
import dev.galacticraft.mod.block.GalacticraftBlocks;
import dev.galacticraft.mod.block.entity.OxygenStorageModuleBlockEntity;
import dev.galacticraft.mod.client.util.CachingSpriteAtlas;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class MachineBakedModel implements FabricBakedModel, BakedModel {
    public static final MachineBakedModel INSTANCE = new MachineBakedModel();

    public static final Identifier MACHINE_MARKER = new Identifier(Constants.MOD_ID, "autogenerated/machine");
    public static final Identifier MACHINE = new Identifier(Constants.MOD_ID, "block/machine");
    public static final Identifier MACHINE_SIDE = new Identifier(Constants.MOD_ID, "block/machine_side");
    public static final Identifier MACHINE_POWER_IN = new Identifier(Constants.MOD_ID, "block/machine_power_input");
    public static final Identifier MACHINE_POWER_OUT = new Identifier(Constants.MOD_ID, "block/machine_power_output");
    public static final Identifier MACHINE_FLUID_IN = new Identifier(Constants.MOD_ID, "block/machine_fluid_input");
    public static final Identifier MACHINE_FLUID_OUT = new Identifier(Constants.MOD_ID, "block/machine_fluid_output");
    public static final Identifier MACHINE_ITEM_IN = new Identifier(Constants.MOD_ID, "block/machine_item_input");
    public static final Identifier MACHINE_ITEM_OUT = new Identifier(Constants.MOD_ID, "block/machine_item_output");

    @ApiStatus.Internal
    public static final CachingSpriteAtlas CACHING_SPRITE_ATLAS = new CachingSpriteAtlas(null);
    @ApiStatus.Internal
    public static final Map<Block, SpriteProvider> SPRITE_PROVIDERS = new HashMap<>();
    @ApiStatus.Internal
    public static final Map<String, Set<String>> IDENTIFIERS = new HashMap<>();
    public static final List<Identifier> TEXTURE_DEPENDENCIES = new LinkedList<>();
    private static final MachineConfiguration CONFIGURATION = new MachineConfiguration();

    protected MachineBakedModel() {}

    public static void register(Block block, SpriteProvider provider) {
        SPRITE_PROVIDERS.put(block, provider);
        Identifier id = Registry.BLOCK.getId(block);
        IDENTIFIERS.putIfAbsent(id.getNamespace(), new HashSet<>());
        IDENTIFIERS.get(id.getNamespace()).add(id.getPath());
    }

    public static void registerDefaults() {
        register(GalacticraftBlocks.ADVANCED_SOLAR_PANEL, new ZAxisSpriteProvider(new Identifier(Constants.MOD_ID, "block/advanced_solar_panel"), false));

        register(GalacticraftBlocks.BASIC_SOLAR_PANEL, new ZAxisSpriteProvider(new Identifier(Constants.MOD_ID, "block/basic_solar_panel"), false));

        register(GalacticraftBlocks.BUBBLE_DISTRIBUTOR, new SingleSpriteProvider(new Identifier(Constants.MOD_ID, "block/oxygen_bubble_distributor")));

        register(GalacticraftBlocks.CIRCUIT_FABRICATOR, new FrontFaceSpriteProvider(new Identifier(Constants.MOD_ID, "block/circuit_fabricator")));

        register(GalacticraftBlocks.COAL_GENERATOR, new FrontFaceSpriteProvider(new Identifier(Constants.MOD_ID, "block/coal_generator")));

        register(GalacticraftBlocks.ENERGY_STORAGE_MODULE, (machine, stack, face, atlas, view, pos) -> {
            if (face == BlockFace.FRONT || face == BlockFace.BACK) {
                double energy;
                if (machine != null) {
                    energy = machine.getCapacitor().getEnergy();
                } else {
                    if (stack.getTag() != null && stack.getTag().contains(Constants.Nbt.ENERGY, NbtType.COMPOUND)) {
                        energy = stack.getTag().getInt(Constants.Nbt.ENERGY);
                    } else {
                        energy = 0;
                    }
                }
                return atlas.apply(new Identifier(Constants.MOD_ID, "block/energy_storage_module_" + (int) ((energy / (double) Galacticraft.CONFIG_MANAGER.get().energyStorageModuleStorageSize()) * 8.0D)));
            }
            return atlas.apply(MACHINE);
        });

        register(GalacticraftBlocks.OXYGEN_COLLECTOR, new SingleSpriteProvider(new Identifier(Constants.MOD_ID, "block/oxygen_collector")));

        register(GalacticraftBlocks.OXYGEN_COMPRESSOR, new ZAxisSpriteProvider(new Identifier(Constants.MOD_ID, "block/oxygen_compressor"), new Identifier(Constants.MOD_ID, "block/oxygen_compressor_back"), true));
        
        register(GalacticraftBlocks.OXYGEN_DECOMPRESSOR, new ZAxisSpriteProvider(new Identifier(Constants.MOD_ID, "block/oxygen_decompressor"), new Identifier(Constants.MOD_ID, "block/oxygen_decompressor_back"), true));

        register(GalacticraftBlocks.OXYGEN_STORAGE_MODULE, (machine, stack, face, atlas, view, pos) -> {
            if (face == BlockFace.FRONT || face == BlockFace.BACK) {
                FluidVolume volume;
                if (machine != null) {
                    volume = machine.getFluidInv().getInvFluid(0);
                } else {
                    if (stack.getTag() != null && stack.getTag().contains(Constants.Nbt.BLOCK_ENTITY_TAG, NbtType.COMPOUND)) {
                        if (stack.getTag().getCompound(Constants.Nbt.BLOCK_ENTITY_TAG).contains("tanks", NbtType.LIST)) {
                            ListTag tag1 = stack.getTag().getCompound(Constants.Nbt.BLOCK_ENTITY_TAG).getList("tanks", NbtType.COMPOUND);
                            if (tag1.size() > 0) {
                                volume = FluidVolume.fromTag(tag1.getCompound(0));
                            } else {
                                volume = FluidVolumeUtil.EMPTY;
                            }
                        } else {
                            volume = FluidVolumeUtil.EMPTY;
                        }
                    } else {
                        volume = FluidVolumeUtil.EMPTY;
                    }
                }
                return atlas.apply(new Identifier(Constants.MOD_ID, "block/oxygen_storage_module_" + volume.getAmount_F().div(OxygenStorageModuleBlockEntity.MAX_CAPACITY).asInt(8, RoundingMode.DOWN)));
            }
            return atlas.apply(MACHINE);
        });

        register(GalacticraftBlocks.REFINERY, new ZAxisSpriteProvider(new Identifier(Constants.MOD_ID, "block/refinery_front"), new Identifier(Constants.MOD_ID, "block/refinery_back"), true));

        register(GalacticraftBlocks.OXYGEN_SEALER, (machine, stack, face, atlas, view, pos) -> {
            if (face == BlockFace.TOP) return atlas.apply(new Identifier(Constants.MOD_ID, "block/oxygen_sealer_top"));
            if (face.isHorizontal()) atlas.apply(MACHINE_SIDE);
            return atlas.apply(MACHINE);
        });

        register(GalacticraftBlocks.ELECTRIC_FURNACE, new FrontFaceSpriteProvider(new Identifier(Constants.MOD_ID, "block/electric_furnace")));
        
        register(GalacticraftBlocks.ELECTRIC_ARC_FURNACE, new FrontFaceSpriteProvider(new Identifier(Constants.MOD_ID, "block/electric_arc_furnace")));
    }

    @ApiStatus.Internal
    public static void setSpriteAtlas(Function<Identifier, Sprite> function) {
        CACHING_SPRITE_ATLAS.setAtlas(function);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        MachineBlockEntity machine = ((MachineBlockEntity) blockView.getBlockEntity(pos));
        assert machine != null;
        context.pushTransform(quad -> transform(machine, state, quad));
        for (Direction direction : Constants.Misc.DIRECTIONS) {
            context.getEmitter().square(direction, 0, 0, 1, 1, 0).emit();
        }
        context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        assert stack.getItem() instanceof BlockItem;
        assert ((BlockItem) stack.getItem()).getBlock() instanceof MachineBlock;
        context.pushTransform(quad -> transformItem(stack, quad));
        for (Direction direction : Constants.Misc.DIRECTIONS) {
            context.getEmitter().square(direction, 0, 0, 1, 1, 0).emit();
        }
        context.popTransform();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return CACHING_SPRITE_ATLAS.apply(MACHINE);
    }

    @Override
    public ModelTransformation getTransformation() {
        return new ModelTransformation(
                new Transformation(new Vector3f(75, 45, 0), new Vector3f(0, 0.25f, 0), new Vector3f(0.375f, 0.375f, 0.375f)),
                new Transformation(new Vector3f(75, 45, 0), new Vector3f(0, 0.25f, 0), new Vector3f(0.375f, 0.375f, 0.375f)),
                new Transformation(new Vector3f(0, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.40f, 0.40f, 0.40f)),
                new Transformation(new Vector3f(0, 45, 0), new Vector3f(0, 0, 0), new Vector3f(0.40f, 0.40f, 0.40f)),
                Transformation.IDENTITY,
                new Transformation(new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.625f, 0.625f, 0.625f)),
                new Transformation(new Vector3f(0, 0, 0), new Vector3f(0, 0.2f, 0), new Vector3f(0.25f, 0.25f, 0.25f)),
                new Transformation(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f))
        );
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @FunctionalInterface
    public interface SpriteProvider {
        SpriteProvider DEFAULT = (machine, stack, face, atlas, view, pos) -> {
            if (face.isHorizontal()) return atlas.apply(MACHINE_SIDE);
            return atlas.apply(MACHINE);
        };

        /**
         * @param machine The machine block entity instance. Will be null in item contexts.
         * @param stack The machine stack being rendered. Will be null in block contexts. DO NOT MODIFY THE STACK
         * @param face The face that is being textured.
         * @param atlas The texture atlas.
         * @param view The position of the machine about to be rendered. Will be null in item contexts.
         * @param pos The position of the machine about to be rendered or t. Will be null in item contexts.
         * @return The appropriate sprite to render for the given face.
         */
        @Contract(pure = true, value = "null,null,_,_,_,_->fail;!null,_,_,_,null,_->fail;")
        @NotNull Sprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<Identifier, Sprite> atlas, @Nullable BlockRenderView view, @Nullable BlockPos pos);
    }

    public static boolean transform(MachineBlockEntity machine, BlockState state, MutableQuadView quad) {
        BlockFace face = BlockFace.toFace(state.get(Properties.HORIZONTAL_FACING), quad.nominalFace());
        quad.spriteBake(0,
                getSprite(face,
                        machine,
                        null,
                        SPRITE_PROVIDERS.getOrDefault(state.getBlock(), SpriteProvider.DEFAULT),
                        machine.getIOConfig().get(face).getAutomationType()),
                MutableQuadView.BAKE_LOCK_UV);
        quad.spriteColor(0, -1, -1, -1, -1);
        return true;
    }

    public static boolean transformItem(ItemStack stack, MutableQuadView quad) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(Constants.Nbt.BLOCK_ENTITY_TAG, NbtType.COMPOUND)) {
            CONFIGURATION.fromTag(tag.getCompound(Constants.Nbt.BLOCK_ENTITY_TAG));
            quad.spriteBake(0,
                    getSprite(BlockFace.toFace(Direction.NORTH, quad.nominalFace()),
                            null,
                            stack,
                            SPRITE_PROVIDERS.getOrDefault(((BlockItem) stack.getItem()).getBlock(), SpriteProvider.DEFAULT),
                            CONFIGURATION.getSideConfiguration().get(BlockFace.toFace(Direction.NORTH, quad.nominalFace())).getAutomationType()),
                    MutableQuadView.BAKE_LOCK_UV);
        } else {
            quad.spriteBake(0, SPRITE_PROVIDERS.getOrDefault(((BlockItem) stack.getItem()).getBlock(), SpriteProvider.DEFAULT)
                    .getSpritesForState(null, stack, BlockFace.toFace(Direction.NORTH, quad.nominalFace()), CACHING_SPRITE_ATLAS, null, null), MutableQuadView.BAKE_LOCK_UV);
        }
        quad.spriteColor(0, -1, -1, -1, -1);
        return true;
    }

    public static Sprite getSprite(BlockFace face, MachineBlockEntity machine, ItemStack stack, SpriteProvider provider, AutomationType type) {
        switch (type) {
            case FLUID_INPUT:
                return CACHING_SPRITE_ATLAS.apply(MACHINE_FLUID_IN);
            case POWER_INPUT:
                return CACHING_SPRITE_ATLAS.apply(MACHINE_POWER_IN);
            case POWER_OUTPUT:
                return CACHING_SPRITE_ATLAS.apply(MACHINE_POWER_OUT);
            case FLUID_OUTPUT:
                return CACHING_SPRITE_ATLAS.apply(MACHINE_FLUID_OUT);
            case ITEM_INPUT:
                return CACHING_SPRITE_ATLAS.apply(MACHINE_ITEM_IN);
            case ITEM_OUTPUT:
                return CACHING_SPRITE_ATLAS.apply(MACHINE_ITEM_OUT);
            default:
                return provider.getSpritesForState(machine, stack, face, CACHING_SPRITE_ATLAS, null, null);
        }
    }

    public static class FrontFaceSpriteProvider implements SpriteProvider {
        private final Identifier sprite;

        public FrontFaceSpriteProvider(Identifier sprite) {
            this.sprite = sprite;
            TEXTURE_DEPENDENCIES.add(sprite);
        }

        @Override
        public @NotNull Sprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<Identifier, Sprite> atlas, @Nullable BlockRenderView view, @Nullable BlockPos pos) {
            if (face == BlockFace.FRONT) return atlas.apply(sprite);
            if (face.isHorizontal()) return atlas.apply(MACHINE_SIDE);
            return atlas.apply(MACHINE);
        }
    }

    public static class SingleSpriteProvider implements SpriteProvider {
        private final Identifier sprite;

        public SingleSpriteProvider(Identifier sprite) {
            this.sprite = sprite;
            TEXTURE_DEPENDENCIES.add(sprite);
        }

        @Override
        public @NotNull Sprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<Identifier, Sprite> atlas, @Nullable BlockRenderView view, @Nullable BlockPos pos) {
            return atlas.apply(sprite);
        }
    }

    public static class ZAxisSpriteProvider implements SpriteProvider {
        private final Identifier front;
        private final Identifier back;
        private final boolean sided;

        public ZAxisSpriteProvider(Identifier sprite, boolean sided) {
            this(sprite, sprite, sided);
        }

        public ZAxisSpriteProvider(Identifier front, Identifier back, boolean sided) {
            this.front = front;
            this.back = back;
            this.sided = sided;
            TEXTURE_DEPENDENCIES.add(front);
            TEXTURE_DEPENDENCIES.add(back);
        }

        @Override
        public @NotNull Sprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<Identifier, Sprite> atlas, @Nullable BlockRenderView view, @Nullable BlockPos pos) {
            if (face == BlockFace.FRONT) return atlas.apply(this.front);
            if (face == BlockFace.BACK) return atlas.apply(this.back);
            if (this.sided && face.isHorizontal()) return atlas.apply(MACHINE_SIDE);
            return atlas.apply(MACHINE);
        }
    }
}
