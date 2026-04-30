package com.timso.client.view;

import com.timso.client.network.GameClient;
import com.timso.common.model.User;

public class PlayerSession {

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
            System.out.println("PlayerSession initialized with gold: " + currentGold);

        }
    }

    public static void setGold(int gold) {
        currentGold = gold;
        if (currentUser != null) {
            currentUser.setGold(gold);
        }
    }

    public static void updateGold(int newGold) {
        currentGold = newGold;
        if (currentUser != null) {
            currentUser.setGold(newGold);
        }
        System.out.println("Gold updated to: " + currentGold);
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
        System.out.println("Current gold: " + currentGold + ", Light skill price: 200");
        if (currentGold >= 200) {
            currentGold -= 200;
            lightSkill++;
            GameClient.getInstance().sendToServer("BUY_SKILL|light|200");
            return true;
        } else {
            System.out.println("Not enough gold! Need 200, have " + currentGold);
            return false;
        }
    }

    public static boolean buyDarkSkill() {
        System.out.println("Current gold: " + currentGold + ", Dark skill price: 300");
        if (currentGold >= 300) {
            currentGold -= 300;
            darkSkill++;
            GameClient.getInstance().sendToServer("BUY_SKILL|dark|300");
            return true;
        }
        return false;
    }

    public static boolean buyFreezeSkill() {
        System.out.println("Current gold: " + currentGold + ", Freeze skill price: 500");
        if (currentGold >= 500) {
            currentGold -= 500;
            freezeSkill++;
            GameClient.getInstance().sendToServer("BUY_SKILL|freeze|500");
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