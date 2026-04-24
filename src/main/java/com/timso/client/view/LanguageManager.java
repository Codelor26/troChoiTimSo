package com.timso.client.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LanguageManager {

    // 👉 lưu vào máy user
    private static final Preferences prefs =
            Preferences.userNodeForPackage(LanguageManager.class);

    private static final String KEY_LANG = "app_lang";

    // 👉 load khi app start
    private static final ObjectProperty<Locale> currentLocale =
            new SimpleObjectProperty<>(
                    new Locale(prefs.get(KEY_LANG, "vi")) // mặc định vi
            );

    // 👉 đổi ngôn ngữ
    public static void setLocale(String langCode) {
        prefs.put(KEY_LANG, langCode); // 🔥 LƯU LẠI
        currentLocale.set(new Locale(langCode));
        ResourceBundle.clearCache();
    }

    public static Locale getLocale() {
        return currentLocale.get();
    }

    public static ObjectProperty<Locale> localeProperty() {
        return currentLocale;
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("language.messages", getLocale());
    }

    public static String getString(String key) {
        return getBundle().getString(key);
    }
}