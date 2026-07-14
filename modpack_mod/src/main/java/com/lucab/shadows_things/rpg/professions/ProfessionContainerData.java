package com.lucab.shadows_things.rpg.professions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;

public class ProfessionContainerData implements ContainerData {
    private final Player player;

    public ProfessionContainerData(Player player) {
        this.player = player;
    }

    @Override
    public int get(int index) {
        ProfessionAttachments att = player.getData(ProfessionAttachments.PROFESSION.get());
        return switch (index) {
            case 0 -> ProfessionHelper.getExperience(player);
            case 1 -> ProfessionHelper.getExperienceRequired(player);
            case 2 -> ProfessionHelper.getPoints(player);
            case 3 -> att.professionLevels.getOrDefault(ProfessionHelper.Professions.BLACKSMITH, 0);
            case 4 -> att.professionLevels.getOrDefault(ProfessionHelper.Professions.FARMER, 0);
            case 5 -> att.professionLevels.getOrDefault(ProfessionHelper.Professions.COOK, 0);
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
    }

    @Override
    public int getCount() {
        return 6;
    }
}
