package com.lucab.shadows_things.rpg.professions;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ProfessionAttachments implements INBTSerializable<CompoundTag> {
    protected int points = 0;
    protected int experience = 0;
    protected final Map<ProfessionHelper.Professions, Integer> professionLevels = new EnumMap<>(ProfessionHelper.Professions.class);

    public ProfessionAttachments() {
        for (ProfessionHelper.Professions profession : ProfessionHelper.Professions.values()) {
            this.professionLevels.put(profession, 0);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("points", this.points);
        nbt.putInt("experience", this.experience);

        CompoundTag levelsTag = new CompoundTag();
        this.professionLevels.forEach((profession, level) -> {
            levelsTag.putInt(profession.name(), level);
        });

        nbt.put("ProfessionLevels", levelsTag);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.points = nbt.getInt("points");
        this.experience = nbt.getInt("experience");

        if (nbt.contains("ProfessionLevels")) {
            CompoundTag levelsTag = nbt.getCompound("ProfessionLevels");

            for (ProfessionHelper.Professions profession : ProfessionHelper.Professions.values()) {
                if (levelsTag.contains(profession.name())) {
                    this.professionLevels.put(profession, levelsTag.getInt(profession.name()));
                } else {
                    this.professionLevels.put(profession, 0);
                }
            }
        }
    }

    public static final Supplier<AttachmentType<ProfessionAttachments>> PROFESSION = ShadowsThings.ATTACHMENT_TYPES
            .register("profession", () -> AttachmentType.serializable(ProfessionAttachments::new).build());

    public static void register() {

    }
}
