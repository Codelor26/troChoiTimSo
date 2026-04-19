package com.timso.client.view;

public class PlayerSession {

    private static int gold = 2000;
    private static int lightSkill = 0;
    private static int darkSkill = 0;
    private static int freezeSkill = 0;

    public static int getGold() {
        return gold;
    }

    public static void addGold(int amount) {
        gold += amount;
    }

    public static int getLightSkill() {
        return lightSkill;
    }

    public static int getDarkSkill() {
        return darkSkill;
    }

    public static int getFreezeSkill() {
        return freezeSkill;
    }

    public static boolean buyLightSkill() {
        if (gold >= 200) {
            gold -= 200;
            lightSkill++;
            return true;
        }
        return false;
    }

    public static boolean buyDarkSkill() {
        if (gold >= 300) {
            gold -= 300;
            darkSkill++;
            return true;
        }
        return false;
    }

    public static boolean buyFreezeSkill() {
        if (gold >= 500) {
            gold -= 500;
            freezeSkill++;
            return true;
        }
        return false;
    }

    public static boolean useLightSkill() {
        if (lightSkill > 0) {
            lightSkill--;
            return true;
        }
        return false;
    }

    public static boolean useDarkSkill() {
        if (darkSkill > 0) {
            darkSkill--;
            return true;
        }
        return false;
    }

    public static boolean useFreezeSkill() {
        if (freezeSkill > 0) {
            freezeSkill--;
            return true;
        }
        return false;
    }
}