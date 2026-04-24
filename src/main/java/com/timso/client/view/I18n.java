package com.timso.client.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;

public class I18n {

    public static StringBinding bind(String key) {
        return Bindings.createStringBinding(
                () -> LanguageManager.getString(key),
                LanguageManager.localeProperty()
        );
    }
}