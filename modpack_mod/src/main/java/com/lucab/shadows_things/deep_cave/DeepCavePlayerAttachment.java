package com.lucab.shadows_things.deep_cave;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.function.Supplier;

public class DeepCavePlayerAttachment implements INBTSerializable<CompoundTag> {
    private BlockPos portalPos;
    private Direction portalDir;

    public void setPortalPos(BlockPos portalPos) {
        this.portalPos = portalPos;
    }

    public void setPortalDir(Direction portalDir) {
        this.portalDir = portalDir;
    }

    public BlockPos getPortalPos() {
        return portalPos;
    }

    public Direction getPortalDir() {
        return portalDir;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (portalPos != null) {
            nbt.put("portalPos", NbtUtils.writeBlockPos(portalPos));
        }
        if (portalDir != null) {
            nbt.putInt("portalDir", portalDir.get3DDataValue());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("portalPos")) {
            NbtUtils.readBlockPos(nbt, "portalPos").ifPresent(pos -> this.portalPos = pos);
        } else {
            portalPos = null;
        }

        if (nbt.contains("portalDir")) {
            portalDir = Direction.from3DDataValue(nbt.getInt("portalDir"));
        } else {
            portalDir = null;
        }
    }

    public static final Supplier<AttachmentType<DeepCavePlayerAttachment>> DEEP_CAVE_ATTACHMENT = ShadowsThings.ATTACHMENT_TYPES
            .register("deep_cave_player_data", () -> AttachmentType.serializable(DeepCavePlayerAttachment::new).build());

    public static void register() {
    }
}
