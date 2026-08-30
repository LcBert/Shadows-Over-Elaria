package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
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
            ByteBufCodecs.VAR_INT, ClassPlayerData::getExperience,
            ClassPlayerData::new
    );

    // ==========
    // Class Data
    // ==========
    private String className = NONE_CLASS;
    private int classTier = 0;
    private int experience = 0;

    public ClassPlayerData() {
    }

    public ClassPlayerData(String className, int classTier, int experience) {
        this.className = className;
        this.classTier = classTier;
        this.experience = experience;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getExperience() {
        return experience;
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

    // ===============
    // Synchronization
    // ===============

    public static ClassPlayerData getClassData(Player player) {
        return player.getData(ClassPlayerData.CLASS_PLAYER_DATA);
    }

    public static void sync(Player player) {
        player.setData(ClassPlayerData.CLASS_PLAYER_DATA.get(), getClassData(player));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag nbt = new CompoundTag();
        // Class Data
        nbt.putString("className", className);
        nbt.putInt("classTier", classTier);
        nbt.putInt("experience", experience);

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
        experience = nbt.getInt("experience");

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
