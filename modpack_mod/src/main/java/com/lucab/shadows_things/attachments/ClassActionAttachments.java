package com.lucab.shadows_things.attachments;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.function.Supplier;

public class ClassActionAttachments implements INBTSerializable<CompoundTag> {
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
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("PrimaryLastUseTick", primaryLastUseTick);
        nbt.putLong("SecondaryLastUseTick", secondaryLastUseTick);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        primaryLastUseTick = nbt.getLong("PrimaryLastUseTick");
        secondaryLastUseTick = nbt.getLong("SecondaryLastUseTick");
    }

    public static final Supplier<AttachmentType<ClassActionAttachments>> CLASS_ACTION = ShadowsThings.ATTACHMENT_TYPES
            .register("class_action", () -> AttachmentType.serializable(ClassActionAttachments::new).build());


    public static void register() {
    }
}
