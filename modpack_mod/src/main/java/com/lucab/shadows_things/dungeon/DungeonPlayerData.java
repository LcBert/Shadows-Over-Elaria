package com.lucab.shadows_things.dungeon;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DungeonPlayerData implements INBTSerializable<CompoundTag> {
    private static final String TAG_PORTAL_POS = "portalPos";
    private static final String TAG_PORTAL_DIR = "portalDir";

    @Nullable
    private BlockPos portalPos;
    @Nullable
    private Direction portalDir;

    public void setPortalPos(@Nullable BlockPos portalPos) {
        this.portalPos = portalPos;
    }

    public @Nullable BlockPos getPortalPos() {
        return portalPos;
    }

    public void setPortalDir(@Nullable Direction portalDir) {
        this.portalDir = portalDir;
    }

    public @Nullable Direction getPortalDir() {
        return portalDir;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (portalPos != null) {
            nbt.put(TAG_PORTAL_POS, NbtUtils.writeBlockPos(portalPos));
        }
        if (portalDir != null) {
            nbt.putInt(TAG_PORTAL_DIR, portalDir.get3DDataValue());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.portalPos = nbt.contains(TAG_PORTAL_POS)
                ? NbtUtils.readBlockPos(nbt, TAG_PORTAL_POS).orElse(null)
                : null;

        this.portalDir = nbt.contains(TAG_PORTAL_DIR)
                ? Direction.from3DDataValue(nbt.getInt(TAG_PORTAL_DIR))
                : null;
    }

    public static final Supplier<AttachmentType<DungeonPlayerData>> DUNGEON_PLAYER_DATA = ShadowsThings.ATTACHMENT_TYPES
            .register("dungeon_player_data", () -> AttachmentType
                    .serializable(DungeonPlayerData::new)
                    .build()
            );

    public static void register() {
    }
}
