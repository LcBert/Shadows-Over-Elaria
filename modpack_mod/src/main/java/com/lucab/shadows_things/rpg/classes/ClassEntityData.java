package com.lucab.shadows_things.rpg.classes;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class ClassEntityData implements INBTSerializable<CompoundTag> {
    public ClassEntityData() {
    }

    // ===========
    // Entity Data
    // ===========
    private final Map<UUID, Float> playersDamage = new HashMap<>();

    public Map<UUID, Float> getPlayersDamage() {
        return playersDamage;
    }

    public void addDamage(UUID uuid, float damage) {
        playersDamage.put(uuid, getDamage(uuid) + damage);
    }

    public float getDamage(UUID uuid) {
        return playersDamage.getOrDefault(uuid, 0.0f);
    }

    // ===============
    // Synchronization
    // ===============
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        CompoundTag damageTag = new CompoundTag();
        for (Map.Entry<UUID, Float> entry : playersDamage.entrySet()) {
            damageTag.putFloat(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("PlayersDamage", damageTag);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.playersDamage.clear();
        if (nbt.contains("PlayersDamage", CompoundTag.TAG_COMPOUND)) {
            CompoundTag damageTag = nbt.getCompound("PlayersDamage");
            for (String key : damageTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    float damage = damageTag.getFloat(key);
                    this.playersDamage.put(uuid, damage);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public static final Supplier<AttachmentType<ClassEntityData>> CLASS_ENTITY_DATA = ShadowsThings.ATTACHMENT_TYPES.register(
            "class_entity_data", () -> AttachmentType
                    .serializable(ClassEntityData::new)
                    .build()
    );

    public static void register() {
    }
}
