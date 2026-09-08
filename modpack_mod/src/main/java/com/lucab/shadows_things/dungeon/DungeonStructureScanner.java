package com.lucab.shadows_things.dungeon;

import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

public class DungeonStructureScanner {
    private static Field templateField;

    static {
        try {
            templateField = SinglePoolElement.class.getDeclaredField("template");
            templateField.setAccessible(true);
        } catch (NoSuchFieldException ignored) {

        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static ResourceLocation resolveElementTemplate(StructurePoolElement element) {
        if (element instanceof SinglePoolElement single) {
            try {
                if (templateField != null) {
                    @SuppressWarnings("unchecked")
                    Either<ResourceLocation, StructureTemplate> templateEither =
                            (Either<ResourceLocation, StructureTemplate>) templateField.get(single);

                    return templateEither.map(
                            res -> res,
                            template -> null // StructureTemplate instances created dynamically in-memory have no direct ResourceLocation
                    );
                }
            } catch (Exception e) {
                return null;
            }
        } else if (element instanceof ListPoolElement listElement) {
            // Handles list_pool_element wrapper defined in JSON pools
            for (StructurePoolElement child : getListElements(listElement)) {
                ResourceLocation found = resolveElementTemplate(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<StructurePoolElement> getListElements(ListPoolElement listElement) {
        try {
            Field elementsField = ListPoolElement.class.getDeclaredField("elements");
            elementsField.setAccessible(true);
            return (List<StructurePoolElement>) elementsField.get(listElement);
        } catch (Exception e) {
            return List.of();
        }
    }
}
