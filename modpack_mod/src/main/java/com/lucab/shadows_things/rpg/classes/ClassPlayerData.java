package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ClassPlayerData implements INBTSerializable<CompoundTag> {
    public static final String NONE_CLASS = "none";
    public static final String WANDERER_CLASS = "wanderer";

    public static final StreamCodec<RegistryFriendlyByteBuf, ClassPlayerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClassPlayerData::getClassName,
            ByteBufCodecs.VAR_INT, ClassPlayerData::getClassTier,
            ClassPlayerData::new
    );

    // ====================
    // Class Data
    // ====================
    private String className = NONE_CLASS;
    private int classTier = 0;

    public ClassPlayerData() {
    }

    public ClassPlayerData(String className, int classTier) {
        this.className = className;
        this.classTier = classTier;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void resetClass() {
        this.className = WANDERER_CLASS;
    }

    public void removeClass() {
        this.className = NONE_CLASS;
    }

    public String getClassName() {
        return className;
    }

    public void setClassTier(int classTier) {
        this.classTier = classTier;
    }

    public int getClassTier() {
        return classTier;
    }

    // =======================
    // Class Actions Cooldown
    // =======================

    private long primaryLastUseTick;
    private long secondaryLastUseTick;

    public void setPrimaryLastUseTick(long primaryLastUseTick) {
        this.primaryLastUseTick = primaryLastUseTick;
    }

    public void setSecondaryLastUseTick(long secondaryLastUseTick) {
        this.secondaryLastUseTick = secondaryLastUseTick;
    }

    public long getPrimaryLastUseTick() {
        return primaryLastUseTick;
    }

    public long getSecondaryLastUseTick() {
        return secondaryLastUseTick;
    }

    public boolean canUsePrimary(int cooldown, long playerTick) {
        return primaryLastUseTick + cooldown <= playerTick;
    }

    public boolean canUseSecondary(int cooldown, long playerTick) {
        return secondaryLastUseTick + cooldown <= playerTick;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag nbt = new CompoundTag();
        // Class Data
        nbt.putString("className", className);
        nbt.putInt("classTier", classTier);

        // Class Actions Cooldown
        nbt.putLong("primaryLastUseTick", primaryLastUseTick);
        nbt.putLong("secondaryLastUseTick", secondaryLastUseTick);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        // Class Data
        className = nbt.getString("className");
        classTier = nbt.getInt("classTier");

        // Class Actions Cooldown
        primaryLastUseTick = nbt.getLong("primaryLastUseTick");
        secondaryLastUseTick = nbt.getLong("secondaryLastUseTick");
    }

    public static final Supplier<AttachmentType<ClassPlayerData>> CLASS_PLAYER_DATA = ShadowsThings.ATTACHMENT_TYPES.register(
            "class_data", () -> AttachmentType
                    .serializable(ClassPlayerData::new)
                    .sync(STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static void register() {
    }
}
