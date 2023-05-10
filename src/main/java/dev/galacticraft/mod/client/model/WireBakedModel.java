/*
 * Copyright (c) 2019-2023 Team Galacticraft
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

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.block.entity.networked.WireBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
@Environment(EnvType.CLIENT)
public class WireBakedModel implements FabricBakedModel, BakedModel {
    private static WireBakedModel instance = null;

    public static final ResourceLocation WIRE_MARKER = new ResourceLocation(Constant.MOD_ID, "autogenerated/aluminum_wire");
    public static final ResourceLocation ALUMINUM_WIRE = new ResourceLocation(Constant.MOD_ID, "block/aluminum_wire");
    private final Mesh down;
    private final Mesh up;
    private final Mesh north;
    private final Mesh south;
    private final Mesh west;
    private final Mesh east;
    private final TextureAtlasSprite sprite;

    protected WireBakedModel(Function<Material, TextureAtlasSprite> textureGetter) {
        this.sprite = textureGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ALUMINUM_WIRE));
        MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();
        emitter.square(Direction.DOWN, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 12, 0).sprite(2, 0, 16, 0).sprite(3, 0, 16, 4).sprite(0, 0, 12, 4).cullFace(Direction.DOWN).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.WEST, 0.6f, 0.4f, 0.4f, 0.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 0, 0).sprite(2, 0, 4, 0).sprite(3, 0, 4, 8).sprite(0, 0, 0, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.EAST, 0.6f, 0.4f, 0.4f, 0.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 0).sprite(2, 0, 8, 0).sprite(3, 0, 8, 8).sprite(0, 0, 4, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.SOUTH, 0.4f, 0.0f, 0.6f, 0.4f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 8, 0).sprite(2, 0, 12, 0).sprite(3, 0, 12, 8).sprite(0, 0, 8, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.NORTH, 0.4f, 0.0f, 0.6f, 0.4f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 0).sprite(2, 0, 8, 0).sprite(3, 0, 8, 8).sprite(0, 0, 4, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        this.down = meshBuilder.build();
        emitter.square(Direction.UP, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 12, 0).sprite(2, 0, 16, 0).sprite(3, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.UP).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.EAST, 0.6f, 1.0f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 0, 8).sprite(2, 0, 4, 8).sprite(3, 0, 4, 16).sprite(0, 0, 0, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.WEST, 0.6f, 1.0f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 8).sprite(2, 0, 8, 8).sprite(3, 0, 8, 16).sprite(0, 0, 4, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.NORTH, 0.4f, 0.6f, 0.6f, 1.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 8, 8).sprite(2, 0, 12, 8).sprite(3, 0, 12, 16).sprite(0, 0, 8, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.SOUTH, 0.4f, 0.6f, 0.6f, 1.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 8).sprite(2, 0, 8, 8).sprite(3, 0, 8, 16).sprite(0, 0, 4, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        this.up = meshBuilder.build();
        emitter.square(Direction.NORTH, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 12, 0).sprite(2, 0, 16, 0).sprite(3, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.NORTH).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.WEST, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 0, 0).sprite(1, 0, 4, 0).sprite(2, 0, 4, 8).sprite(3, 0, 0, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.EAST, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 0).sprite(1, 0, 8, 0).sprite(2, 0, 8, 8).sprite(3, 0, 4, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.DOWN, 0.4f, 0.0f, 0.6f, 0.4f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 8, 0).sprite(2, 0, 12, 0).sprite(3, 0, 12, 8).sprite(0, 0, 8, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.UP, 0.4f, 0.6f, 0.6f, 1.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 0).sprite(2, 0, 8, 0).sprite(3, 0, 8, 8).sprite(0, 0, 4, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        this.north = meshBuilder.build();
        emitter.square(Direction.SOUTH, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 12, 0).sprite(2, 0, 16, 0).sprite(3, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.SOUTH).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.EAST, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 0, 8).sprite(1, 0, 4, 8).sprite(2, 0, 4, 16).sprite(3, 0, 0, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.WEST, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 8).sprite(1, 0, 8, 8).sprite(2, 0, 8, 16).sprite(3, 0, 4, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.UP, 0.4f, 0.0f, 0.6f, 0.4f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 8, 8).sprite(2, 0, 12, 8).sprite(3, 0, 12, 16).sprite(0, 0, 8, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.DOWN, 0.4f, 0.6f, 0.6f, 1.0f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(1, 0, 4, 8).sprite(2, 0, 8, 8).sprite(3, 0, 8, 16).sprite(0, 0, 4, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        this.south = meshBuilder.build();
        emitter.square(Direction.WEST, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.WEST).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.NORTH, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 0, 0).sprite(1, 0, 4, 0).sprite(2, 0, 4, 8).sprite(3, 0, 0, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.SOUTH, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 0).sprite(1, 0, 8, 0).sprite(2, 0, 8, 8).sprite(3, 0, 4, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.UP, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 8, 0).sprite(1, 0, 12, 0).sprite(2, 0, 12, 8).sprite(3, 0, 8, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.DOWN, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 0).sprite(1, 0, 8, 0).sprite(2, 0, 8, 8).sprite(3, 0, 4, 8).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        this.west = meshBuilder.build();
        emitter.square(Direction.EAST, 0.4f, 0.4f, 0.6f, 0.6f, 0.0f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).cullFace(Direction.EAST).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.SOUTH, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 0, 8).sprite(1, 0, 4, 8).sprite(2, 0, 4, 16).sprite(3, 0, 0, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.NORTH, 0.0f, 0.4f, 0.4f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 8).sprite(1, 0, 8, 8).sprite(2, 0, 8, 16).sprite(3, 0, 4, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.DOWN, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 8, 8).sprite(1, 0, 12, 8).sprite(2, 0, 12, 16).sprite(3, 0, 8, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        emitter.square(Direction.UP, 0.6f, 0.4f, 1.0f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 4, 8).sprite(1, 0, 8, 8).sprite(2, 0, 8, 16).sprite(3, 0, 4, 16).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        this.east = meshBuilder.build();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        WireBlockEntity wire = ((WireBlockEntity) blockView.getBlockEntity(pos));
        Consumer<Mesh> meshConsumer = context.meshConsumer();
        QuadEmitter emitter = context.getEmitter();
        assert wire != null;

        wire.calculateConnections();

        if (wire.getConnections()[0]) {
            meshConsumer.accept(this.down);
        } else {
            emitter.square(Direction.DOWN, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        }
        if (wire.getConnections()[1]) {
            meshConsumer.accept(this.up);
        } else {
            emitter.square(Direction.UP, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        }
        if (wire.getConnections()[2]) {
            meshConsumer.accept(this.north);
        } else {
            emitter.square(Direction.NORTH, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        }
        if (wire.getConnections()[3]) {
            meshConsumer.accept(this.south);
        } else {
            emitter.square(Direction.SOUTH, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        }
        if (wire.getConnections()[4]) {
            meshConsumer.accept(this.west);
        } else {
            emitter.square(Direction.WEST, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        }
        if (wire.getConnections()[5]) {
            meshConsumer.accept(this.east);
        } else {
            emitter.square(Direction.EAST, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteColor(0, -1, -1, -1, -1).sprite(0, 0, 12, 0).sprite(1, 0, 16, 0).sprite(2, 0, 16, 4).sprite(3, 0, 12, 4).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();
        for (Direction direction : Constant.Misc.DIRECTIONS) emitter.square(direction, 0.4f, 0.4f, 0.6f, 0.6f, 0.4f).spriteBake(0, this.sprite, MutableQuadView.BAKE_NORMALIZED & MutableQuadView.BAKE_LOCK_UV).emit();
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
        return this.sprite;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public static WireBakedModel getInstance(Function<Material, TextureAtlasSprite> spriteFunction) {
        if (instance == null) {
            return instance = new WireBakedModel(spriteFunction);
        }
        return instance;
    }

    public static void invalidate() {
        instance = null;
    }
}
