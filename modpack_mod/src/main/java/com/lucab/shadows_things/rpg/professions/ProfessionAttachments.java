package com.lucab.shadows_things.rpg.professions;

import com.lucab.shadows_things.ShadowsThings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ProfessionAttachments implements INBTSerializable<CompoundTag> {
    public record Progress(int level, int experience) {
        public static final Codec<Progress> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("level").forGetter(Progress::level),
                Codec.INT.fieldOf("experience").forGetter(Progress::experience)
        ).apply(inst, Progress::new));

        public static final StreamCodec<ByteBuf, Progress> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Progress::level,
                ByteBufCodecs.VAR_INT, Progress::experience,
                Progress::new
        );
    }

    private static final StreamCodec<ByteBuf, ProfessionHelper.Professions> PROFESSION_ENUM_STREAM_CODEC =
            ByteBufCodecs.idMapper(
                    id -> ProfessionHelper.Professions.values()[id],
                    Enum::ordinal
            );

    private static final StreamCodec<ByteBuf, Map<ProfessionHelper.Professions, Progress>> MAP_STREAM_CODEC =
            ByteBufCodecs.map(
                    size -> new EnumMap<>(ProfessionHelper.Professions.class),
                    PROFESSION_ENUM_STREAM_CODEC,
                    Progress.STREAM_CODEC
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProfessionAttachments> STREAM_CODEC = StreamCodec.of(
            (buf, attachment) -> MAP_STREAM_CODEC.encode(buf, attachment.professions),
            buf -> {
                ProfessionAttachments attachment = new ProfessionAttachments();
                attachment.professions.putAll(MAP_STREAM_CODEC.decode(buf));
                return attachment;
            }
    );

    private final Map<ProfessionHelper.Professions, Progress> professions = new EnumMap<>(ProfessionHelper.Professions.class);

    public ProfessionAttachments() {
        for (ProfessionHelper.Professions profession : ProfessionHelper.Professions.values()) {
            this.professions.put(profession, new Progress(0, 0));
        }
    }

    public Map<ProfessionHelper.Professions, Progress> getProfessionsMap() {
        return Collections.unmodifiableMap(this.professions);
    }

    // ==========================================
    // STATIC PLAYER HELPERS (AUTO-DIRTY & SYNC)
    // ==========================================
    public static void sync(Player player) {
        if (!player.level().isClientSide) {
            player.setData(PROFESSION.get(), get(player));
        }
    }

    public static ProfessionAttachments get(Player player) {
        return player.getData(PROFESSION.get());
    }

    // ==============================================
    // INSTANCE MUTATORS (SET / ADD / REMOVE / RESET)
    // ==============================================
    public void setProgress(ProfessionHelper.Professions profession, int level, int experience) {
        int clampedLevel = Mth.clamp(level, 0, ProfessionHelper.MAX_PROFESSION_LEVEL);
        int clampedExp = Math.max(0, experience);

        if (clampedLevel >= ProfessionHelper.MAX_PROFESSION_LEVEL) {
            clampedExp = 0;
        }

        professions.put(profession, new Progress(clampedLevel, clampedExp));
    }

    public void setLevel(ProfessionHelper.Professions profession, int level) {
        int exp = getExperience(profession);
        setProgress(profession, level, exp);
    }

    public void setExperience(ProfessionHelper.Professions profession, int experience) {
        int level = getLevel(profession);
        experience = Math.clamp(experience, 0, getRequiredExpForLevel(level));
        setProgress(profession, level, experience);
    }

    public void addExperience(ProfessionHelper.Professions profession, int amount) {
        if (amount <= 0 || isMaxLevel(profession)) return;

        Progress current = getProgress(profession);
        int newExp = current.experience() + amount;
        int currentLevel = current.level();

        while (currentLevel < ProfessionHelper.MAX_PROFESSION_LEVEL && newExp >= getRequiredExpForLevel(currentLevel)) {
            newExp -= getRequiredExpForLevel(currentLevel);
            currentLevel++;
        }

        if (currentLevel >= ProfessionHelper.MAX_PROFESSION_LEVEL) {
            newExp = 0;
        }

        setProgress(profession, currentLevel, newExp);
    }

    public void removeExperience(ProfessionHelper.Professions profession, int amount) {
        if (amount <= 0) return;

        Progress current = getProgress(profession);
        int currentExp = current.experience() - amount;
        int currentLevel = current.level();

        while (currentExp < 0 && currentLevel > 0) {
            currentLevel--;
            currentExp += getRequiredExpForLevel(currentLevel);
        }

        if (currentLevel == 0 && currentExp < 0) {
            currentExp = 0;
        }

        setProgress(profession, currentLevel, currentExp);
    }

    public void addLevel(ProfessionHelper.Professions profession, int levels) {
        if (levels <= 0) return;
        setLevel(profession, getLevel(profession) + levels);
    }

    public void removeLevel(ProfessionHelper.Professions profession, int levels) {
        if (levels <= 0) return;
        setLevel(profession, getLevel(profession) - levels);
    }

    public void resetProfession(ProfessionHelper.Professions profession) {
        professions.put(profession, new Progress(0, 0));
    }

    public void resetAll() {
        for (ProfessionHelper.Professions profession : ProfessionHelper.Professions.values()) {
            resetProfession(profession);
        }
    }

    // ================
    // INSTANCE GETTERS
    // ================
    public Progress getProgress(ProfessionHelper.Professions profession) {
        return professions.getOrDefault(profession, new Progress(0, 0));
    }

    public int getLevel(ProfessionHelper.Professions profession) {
        return getProgress(profession).level;
    }

    public int getExperience(ProfessionHelper.Professions profession) {
        return getProgress(profession).experience;
    }

    public boolean isMaxLevel(ProfessionHelper.Professions profession) {
        return getLevel(profession) >= ProfessionHelper.MAX_PROFESSION_LEVEL;
    }

    public float getExperienceProgress(ProfessionHelper.Professions profession) {
        if (isMaxLevel(profession)) return 1.0F;
        int reqExp = getRequiredExpForLevel(getLevel(profession));
        return reqExp <= 0 ? 0.0F : Mth.clamp((float) getExperience(profession) / reqExp, 0.0F, 1.0F);
    }

    public static int getRequiredExpForLevel(int level) {
        if (level < 0) return 0;
        if (level >= ProfessionHelper.MAX_PROFESSION_LEVEL) return 0;

        // Configurable parameters
        final int baseExp = 100;
        final double exponent = 2.0D; // Modifiable: 1.2 (gentle), 1.5 (standard RPG), 2.0 (quadratic/steep)

        // Formula: baseExp * (level + 1)^exponent
        return (int) Math.round(baseExp * Math.pow(level + 1, exponent));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        for (Map.Entry<ProfessionHelper.Professions, Progress> entry : this.professions.entrySet()) {
            Progress.CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue())
                    .resultOrPartial()
                    .ifPresent(tag -> nbt.put(entry.getKey().name(), tag));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        for (ProfessionHelper.Professions prof : ProfessionHelper.Professions.values()) {
            if (nbt.contains(prof.name())) {
                Progress.CODEC.parse(NbtOps.INSTANCE, nbt.get(prof.name()))
                        .resultOrPartial()
                        .ifPresent(progress -> professions.put(prof, progress));
            }
        }
    }

    public static final Supplier<AttachmentType<ProfessionAttachments>> PROFESSION = ShadowsThings.ATTACHMENT_TYPES
            .register("profession", () -> AttachmentType.serializable(ProfessionAttachments::new)
                    .copyOnDeath()
                    .sync(STREAM_CODEC)
                    .build()
            );

    public static void register() {
    }
}
