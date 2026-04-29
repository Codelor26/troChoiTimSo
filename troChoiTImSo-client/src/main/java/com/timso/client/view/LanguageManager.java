package com.timso.client.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.prefs.Preferences;

public class LanguageManager {

    private static LanguageManager instance;
    private Properties props = new Properties();
    private String currentLang;

    private static final String PREF_KEY = "game_language";
    private final Preferences prefs = Preferences.userNodeForPackage(LanguageManager.class);

    private LanguageManager() {
        currentLang = prefs.get(PREF_KEY, "vi");
        loadBundle(currentLang);
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public String getString(String key) {
        return props.getProperty(key, "!" + key + "!");
    }

    public void setLocale(String lang) {
        this.currentLang = lang;
        prefs.put(PREF_KEY, lang);
        loadBundle(lang);
    }

    public String getCurrentLang() {
        return currentLang;
    }

    public boolean isVietnamese() {
        return "vi".equals(currentLang);
    }

    private void loadBundle(String lang) {
        String path = "/i18n/messages_" + lang + ".properties";
        try (InputStream is = LanguageManager.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[LanguageManager] File not found: " + path);
                return;
            }
            Properties p = new Properties();
            p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            this.props = p;
        } catch (IOException e) {
            System.err.println("[LanguageManager] Error loading " + path + ": " + e.getMessage());
        }
    }
}
