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

package dev.galacticraft.impl.universe.display.type.ring;

import com.mojang.serialization.Codec;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.api.universe.display.ring.CelestialRingDisplayType;
import dev.galacticraft.impl.universe.display.config.ring.DefaultCelestialRingDisplayConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Vector3f;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EmptyCelestialRingDisplayType extends CelestialRingDisplayType<DefaultCelestialRingDisplayConfig> {
    public static final EmptyCelestialRingDisplayType INSTANCE = new EmptyCelestialRingDisplayType(DefaultCelestialRingDisplayConfig.CODEC);

    public EmptyCelestialRingDisplayType(Codec<DefaultCelestialRingDisplayConfig> codec) {
        super(codec);
    }

    @Override
    public boolean render(CelestialBody<?, ?> body, GuiGraphics graphics, int count, Vector3f systemOffset, float lineScale, float alpha, double mouseX, double mouseY, float delta, Consumer<Supplier<ShaderInstance>> shaderSetter, DefaultCelestialRingDisplayConfig config) {
        return false;
    }
}
