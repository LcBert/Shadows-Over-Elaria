package com.lucab.shadows_things.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DeepCaveEffects extends DimensionSpecialEffects {
    public DeepCaveEffects() {
        super(-128.0F, true, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        return new Vec3(0.1D, 0.1D, 0.1D);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return true;
    }


}
