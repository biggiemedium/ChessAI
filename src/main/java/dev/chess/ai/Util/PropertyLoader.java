package dev.chess.ai.Util;

import dev.chess.ai.Util.Annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

public class PropertyLoader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream in = PropertyLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(Object target) {
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Value.class)) {
                Value annotation = field.getAnnotation(Value.class);
                String val = properties.getProperty(annotation.key());
                if (val != null) {
                    try {
                        field.setAccessible(true);
                        field.set(target, val);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
