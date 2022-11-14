/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

import com.mojang.math.Vector3f;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.block.entity.PipeWalkwayBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
@Environment(EnvType.CLIENT)
public class PipeWalkwayBakedModel implements FabricBakedModel, BakedModel {
    private static PipeWalkwayBakedModel instance = null;
    public static final ResourceLocation PIPE_WALKWAY_MARKER = new ResourceLocation(Constant.MOD_ID, "autogenerated/pipe_walkway");
    public static final ResourceLocation WALKWAY_PLATFORM = new ResourceLocation(Constant.MOD_ID, "block/walkway");
    public static final Map<DyeColor, Material> COLOR_SPRITE_ID_MAP = Util.make(new EnumMap<>(DyeColor.class), map -> {
        for (DyeColor color : DyeColor.values()) {
            map.put(color, new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(Constant.MOD_ID, "block/glass_fluid_pipe/" + color.getName())));
        }
    });

    public final Map<DyeColor, TextureAtlasSprite> colorSpriteMap = new EnumMap<>(DyeColor.class);
    private final BakedModel walkway;
    private final Mesh down;
    private final Mesh up;
    private final Mesh north;
    private final Mesh south;
    private final Mesh west;
    private final Mesh east;

    protected PipeWalkwayBakedModel(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer) {
        this.walkway = loader.getModel(WALKWAY_PLATFORM).bake(loader, textureGetter, rotationContainer, WALKWAY_PLATFORM);
        for (DyeColor color : DyeColor.values()) {
            this.colorSpriteMap.put(color, textureGetter.apply(COLOR_SPRITE_ID_MAP.get(color)));
        }
        MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();
        emitter.square(Direction.DOWN, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 12, 0).sprite(2, 0, 16, 0).sprite(3, 0, 16, 4).sprite(0, 0, 12, 4).cullFace(Direction.DOWN).emit();
        emitter.square(Direction.WEST, 0.6f, 0.4f, 0.4f, 0.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 0, 0).sprite(2, 0, 4, 0).sprite(3, 0, 4, 8).sprite(0, 0, 0, 8).emit();
        emitter.square(Direction.EAST, 0.6f, 0.4f, 0.4f, 0.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 0).sprite(2, 0, 8, 0).sprite(3, 0, 8, 8).sprite(0, 0, 4, 8).emit();
        emitter.square(Direction.SOUTH, 0.4f, 0.0f, 0.6f, 0.4f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 8, 0).sprite(2, 0, 12, 0).sprite(3, 0, 12, 8).sprite(0, 0, 8, 8).emit();
        emitter.square(Direction.NORTH, 0.4f, 0.0f, 0.6f, 0.4f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 0).sprite(2, 0, 8, 0).sprite(3, 0, 8, 8).sprite(0, 0, 4, 8).emit();
        this.down = meshBuilder.build();
        emitter.square(Direction.UP, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 12, 0).sprite(2, 0, 16, 0).sprite(3, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.UP).emit();
        emitter.square(Direction.EAST, 0.6f, 1.0f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 0, 8).sprite(2, 0, 4, 8).sprite(3, 0, 4, 16).sprite(0, 0, 0, 16).emit();
        emitter.square(Direction.WEST, 0.6f, 1.0f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 8).sprite(2, 0, 8, 8).sprite(3, 0, 8, 16).sprite(0, 0, 4, 16).emit();
        emitter.square(Direction.NORTH, 0.4f, 0.6f, 0.6f, 1.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 8, 8).sprite(2, 0, 12, 8).sprite(3, 0, 12, 16).sprite(0, 0, 8, 16).emit();
        emitter.square(Direction.SOUTH, 0.4f, 0.6f, 0.6f, 1.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 8).sprite(2, 0, 8, 8).sprite(3, 0, 8, 16).sprite(0, 0, 4, 16).emit();
        this.up = meshBuilder.build();
        emitter.square(Direction.NORTH, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 12, 0).sprite(2, 0, 16, 0).sprite(3, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.NORTH).emit();
        emitter.square(Direction.WEST, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 0, 0).sprite(1, 0, 4, 0).sprite(2, 0, 4, 8).sprite(3, 0, 0, 8).emit();
        emitter.square(Direction.EAST, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 0).sprite(1, 0, 8, 0).sprite(2, 0, 8, 8).sprite(3, 0, 4, 8).emit();
        emitter.square(Direction.DOWN, 0.4f, 0.0f, 0.6f, 0.4f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 8, 0).sprite(2, 0, 12, 0).sprite(3, 0, 12, 8).sprite(0, 0, 8, 8).emit();
        emitter.square(Direction.UP, 0.4f, 0.6f, 0.6f, 1.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 0).sprite(2, 0, 8, 0).sprite(3, 0, 8, 8).sprite(0, 0, 4, 8).emit();
        this.north = meshBuilder.build();
        emitter.square(Direction.SOUTH, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 12, 0).sprite(2, 0, 16, 0).sprite(3, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.SOUTH).emit();
        emitter.square(Direction.EAST, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 0, 8).sprite(1, 0, 4, 8).sprite(2, 0, 4, 16).sprite(3, 0, 0, 16).emit();
        emitter.square(Direction.WEST, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 8).sprite(1, 0, 8, 8).sprite(2, 0, 8, 16).sprite(3, 0, 4, 16).emit();
        emitter.square(Direction.UP, 0.4f, 0.0f, 0.6f, 0.4f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 8, 8).sprite(2, 0, 12, 8).sprite(3, 0, 12, 16).sprite(0, 0, 8, 16).emit();
        emitter.square(Direction.DOWN, 0.4f, 0.6f, 0.6f, 1.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 8).sprite(2, 0, 8, 8).sprite(3, 0, 8, 16).sprite(0, 0, 4, 16).emit();
        this.south = meshBuilder.build();
        emitter.square(Direction.WEST, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.WEST).emit();
        emitter.square(Direction.NORTH, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 0, 0).sprite(1, 0, 4, 0).sprite(2, 0, 4, 8).sprite(3, 0, 0, 8).emit();
        emitter.square(Direction.SOUTH, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 0).sprite(1, 0, 8, 0).sprite(2, 0, 8, 8).sprite(3, 0, 4, 8).emit();
        emitter.square(Direction.UP, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 8, 0).sprite(1, 0, 12, 0).sprite(2, 0, 12, 8).sprite(3, 0, 8, 8).emit();
        emitter.square(Direction.DOWN, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 0).sprite(1, 0, 8, 0).sprite(2, 0, 8, 8).sprite(3, 0, 4, 8).emit();
        this.west = meshBuilder.build();
        emitter.square(Direction.EAST, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.EAST).emit();
        emitter.square(Direction.SOUTH, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 0, 8).sprite(1, 0, 4, 8).sprite(2, 0, 4, 16).sprite(3, 0, 0, 16).emit();
        emitter.square(Direction.NORTH, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 8).sprite(1, 0, 8, 8).sprite(2, 0, 8, 16).sprite(3, 0, 4, 16).emit();
        emitter.square(Direction.DOWN, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 8, 8).sprite(1, 0, 12, 8).sprite(2, 0, 12, 16).sprite(3, 0, 8, 16).emit();
        emitter.square(Direction.UP, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 8).sprite(1, 0, 8, 8).sprite(2, 0, 8, 16).sprite(3, 0, 4, 16).emit();
        this.east = meshBuilder.build();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        PipeWalkwayBlockEntity walkway = ((PipeWalkwayBlockEntity) blockView.getBlockEntity(pos));
        Consumer<Mesh> meshConsumer = context.meshConsumer();
        QuadEmitter emitter = context.getEmitter();
        assert walkway != null;
        assert walkway.getDirection() != null;

        int x = 0;
        int y = 0;
        switch (walkway.getDirection()) {
            case DOWN -> x = 180;
            case NORTH -> x = 270;
            case SOUTH -> x = 90;
            case EAST -> {
                x = 90;
                y = 90;
            }
            case WEST -> {
                x = 90;
                y = 270;
            }
        }

        WalkwayBakedModel.Transform.INSTANCE.setQuaternions(Vector3f.XP.rotationDegrees(x), Vector3f.YP.rotationDegrees(y));
        context.pushTransform(WalkwayBakedModel.Transform.INSTANCE);
        context.fallbackConsumer().accept(this.walkway);
        context.popTransform();

        ColorTransform.INSTANCE.setSprite(this.colorSpriteMap.get(walkway.getColor()));
        context.pushTransform(ColorTransform.INSTANCE);
        if (walkway.getConnections()[0]) {
            meshConsumer.accept(this.down);
        } else {
            emitter.square(Direction.DOWN, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).emit();
        }
        if (walkway.getConnections()[1]) {
            meshConsumer.accept(this.up);
        } else {
            emitter.square(Direction.UP, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).emit();
        }
        if (walkway.getConnections()[2]) {
            meshConsumer.accept(this.north);
        } else {
            emitter.square(Direction.NORTH, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).emit();
        }
        if (walkway.getConnections()[3]) {
            meshConsumer.accept(this.south);
        } else {
            emitter.square(Direction.SOUTH, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).emit();
        }
        if (walkway.getConnections()[4]) {
            meshConsumer.accept(this.west);
        } else {
            emitter.square(Direction.WEST, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).emit();
        }
        if (walkway.getConnections()[5]) {
            meshConsumer.accept(this.east);
        } else {
            emitter.square(Direction.EAST, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).emit();
        }
        context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();
        for (Direction direction : Constant.Misc.DIRECTIONS) emitter.square(direction, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteBake(0, this.colorSpriteMap.get(DyeColor.WHITE), MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.colorSpriteMap.get(DyeColor.WHITE);
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public static PipeWalkwayBakedModel getInstance(ModelBakery loader, Function<Material, TextureAtlasSprite> spriteFunction, ModelState rotationContainer) {
        if (instance == null) {
            return instance = new PipeWalkwayBakedModel(loader, spriteFunction, rotationContainer);
        }
        return instance;
    }

    public static void invalidate() {
        instance = null;
    }

    private enum ColorTransform implements RenderContext.QuadTransform {
        INSTANCE;
        private TextureAtlasSprite sprite = null;

        public void setSprite(TextureAtlasSprite sprite) {
            this.sprite = sprite;
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            quad.spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV);
            return true;
        }
    }
}
