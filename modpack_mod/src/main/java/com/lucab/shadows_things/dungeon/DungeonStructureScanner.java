package com.lucab.shadows_things.dungeon;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class DungeonStructureScanner {
    private DungeonStructureScanner() {
    }

    public static final Set<UUID> HIGHLIGHTED_PLAYERS = new HashSet<>();
    private static Field templateField;
    private static Field elementsField;

    static {
        try {
            for (Field f : SinglePoolElement.class.getDeclaredFields()) {
                if (Either.class.isAssignableFrom(f.getType())) {
                    templateField = f;
                    templateField.setAccessible(true);
                    break;
                }
            }
            for (Field f : ListPoolElement.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType())) {
                    elementsField = f;
                    elementsField.setAccessible(true);
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean toggleHighlight(UUID playerUuid) {
        if (HIGHLIGHTED_PLAYERS.contains(playerUuid)) {
            HIGHLIGHTED_PLAYERS.remove(playerUuid);
            return false;
        } else {
            HIGHLIGHTED_PLAYERS.add(playerUuid);
            return true;
        }
    }

    public static void clearHighlights() {
        HIGHLIGHTED_PLAYERS.clear();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static ResourceLocation resolveElementTemplate(StructurePoolElement element) {
        if (element instanceof SinglePoolElement single && templateField != null) {
            try {
                Either<ResourceLocation, StructureTemplate> either =
                        (Either<ResourceLocation, StructureTemplate>) templateField.get(single);
                return either.map(location -> location, template -> null);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        } else if (element instanceof ListPoolElement listElement) {
            for (StructurePoolElement child : getListElements(listElement)) {
                ResourceLocation found = resolveElementTemplate(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<StructurePoolElement> getListElements(ListPoolElement listElement) {
        if (elementsField == null) return Collections.emptyList();
        try {
            return (List<StructurePoolElement>) elementsField.get(listElement);
        } catch (ReflectiveOperationException e) {
            return Collections.emptyList();
        }
    }
}
