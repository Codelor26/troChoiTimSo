package com.timso.client.view;

import com.timso.common.model.User;

public class PlayerSession {

    private static int gold;
    private static int lightSkill;
    private static int darkSkill;
    private static int freezeSkill;
    private static User currentUser;
    private static int currentGold;
    private static String currentUsername;

    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            currentUsername = user.getUserName();
            currentGold = user.getGold();
        }
    }

    public static void setGold(int gold) {
        currentGold = gold;
        if (currentUser != null) {
            currentUser.setGold(gold);
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getGold() {
        return currentGold;
    }

    public static void addGold(int amount) {
        currentGold += amount;
        if (currentUser != null) {
            currentUser.setGold(currentGold);
        }
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