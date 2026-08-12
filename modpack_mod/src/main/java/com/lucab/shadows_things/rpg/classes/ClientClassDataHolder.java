package com.lucab.shadows_things.rpg.classes;

import java.util.Objects;

public class ClientClassDataHolder {
    private static String className = ClassManager.NULL;
    private static int tier = 0;

    public static void setPlayerClass(String name, int t) {
        className = name;
        tier = t;
    }

    public static String getClassName() {
        return className;
    }

    public static int getTier() {
        return tier;
    }

    public static boolean hasClass() {
        return !Objects.equals(className, ClassManager.NULL) && !Objects.equals(className, ClassManager.WANDERER);
    }
}
