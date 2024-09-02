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

package dev.galacticraft.api.perlin.generator;

import dev.galacticraft.api.perlin.FishyNoise;
import dev.galacticraft.api.perlin.NoiseModule;

import java.util.Random;

public class Gradient extends NoiseModule
{
    private final FishyNoise noiseGen;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final int numOctaves;
    private final float persistance;

    public Gradient(long seed, int nOctaves, float p)
    {
        this.numOctaves = nOctaves;
        this.persistance = p;
        final Random rand = new Random(seed);
        this.offsetX = rand.nextFloat() / 2 + 0.01F;
        this.offsetY = rand.nextFloat() / 2 + 0.01F;
        this.offsetZ = rand.nextFloat() / 2 + 0.01F;
        this.noiseGen = new FishyNoise(seed);
    }

    @Override
    public float getNoise(float i)
    {
        i *= this.frequencyX;
        float val = 0;
        float curAmplitude = this.amplitude;
        for (int n = 0; n < this.numOctaves; n++)
        {
            val += this.noiseGen.noise2d(i + this.offsetX, this.offsetY) * curAmplitude;
            i *= 2;
            curAmplitude *= this.persistance;
        }
        return val;
    }

    @Override
    public float getNoise(float i, float j)
    {
        if (this.numOctaves == 1)
        {
            return this.noiseGen.noise2d(i * this.frequencyX + this.offsetX, j * this.frequencyY + this.offsetY) * this.amplitude;
        }

        i *= this.frequencyX;
        j *= this.frequencyY;
        float val = 0;
        float curAmplitude = this.amplitude;
        for (int n = 0; n < this.numOctaves; n++)
        {
            val += this.noiseGen.noise2d(i + this.offsetX, j + this.offsetY) * curAmplitude;
            i *= 2;
            j *= 2;
            curAmplitude *= this.persistance;
        }
        return val;
    }

    @Override
    public float getNoise(float i, float j, float k)
    {
        if (this.numOctaves == 1)
        {
            return this.noiseGen.noise3d(i * this.frequencyX + this.offsetX, j * this.frequencyY + this.offsetY, k * this.frequencyZ + this.offsetZ) * this.amplitude;
        }

        i *= this.frequencyX;
        j *= this.frequencyY;
        k *= this.frequencyZ;
        float val = 0;
        float curAmplitude = this.amplitude;
        for (int n = 0; n < this.numOctaves; n++)
        {
            val += this.noiseGen.noise3d(i + this.offsetX, j + this.offsetY, k + this.offsetZ) * curAmplitude;
            i *= 2;
            j *= 2;
            k *= 2;
            curAmplitude *= this.persistance;
        }
        return val;
    }
}