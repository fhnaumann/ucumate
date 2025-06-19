package io.github.fhnaumann;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Felix Naumann
 */
public class ErrorMessages {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("error_messages", Locale.ENGLISH);

    public static String get(String key, Object... args) {
        String pattern = BUNDLE.getString(key);
        return MessageFormat.format(pattern, args);
    }
}
