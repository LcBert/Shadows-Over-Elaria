package com.lucab.shadows_things.rpg.gems;

import com.lucab.shadows_things.ShadowsThings;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SocketRegistries {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SocketDataComponent>> SOCKET_HOLDER =
            ShadowsThings.DATA_COMPONENTS.register("sockets", () -> DataComponentType.<SocketDataComponent>builder()
                    .persistent(SocketDataComponent.CODEC)
                    .networkSynchronized(SocketDataComponent.STREAM_CODEC)
                    .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GemData>> GEM_DATA =
            ShadowsThings.DATA_COMPONENTS.register("gem_data", () -> DataComponentType.<GemData>builder()
                    .persistent(GemData.CODEC)
                    .networkSynchronized(GemData.STREAM_CODEC)
                    .build()
            );

    public static void register() {
    }
}
