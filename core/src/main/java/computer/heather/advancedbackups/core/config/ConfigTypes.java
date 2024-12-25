package computer.heather.advancedbackups.core.config;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class ConfigTypes {

    private String key;


    public ConfigTypes(String key, BiConsumer<String, ConfigTypes> manager) {
        this.key = key;
        manager.accept(key, this);
    }

    public String getKey() {
        return key;
    }

    public abstract ConfigValidationEnum validate(String in);

    public abstract void load(String in);

    public abstract String save();


    public static enum ConfigValidationEnum {
        OUT_OF_RANGE,
        INCORRECT_FORMAT,
        VALID;

        public String getError() {
            switch (this) {
                case OUT_OF_RANGE:
                    return "Value not within specified parameters!";
                case INCORRECT_FORMAT:
                    return "Value is of an incorrect type!";
                default:
                    return "";
            }
        }
    }


    public static class LongValue extends ConfigTypes {
        private long value;
        private long min;
        private long max;

        public LongValue(String key, long defaultValue, long min, long max, BiConsumer<String, ConfigTypes> manager) {
            super(key, manager);
            this.value = defaultValue;
            this.min = min;
            this.max = max;
        }

        public long get() {
            return value;
        }

        @Override
        public void load(String in) {
            value = Long.parseLong(in);
        }

        @Override
        public String save() {
            return Long.toString(value);
        }

        @Override
        public ConfigValidationEnum validate(String in) {
            long i;
            try {
                i = Long.parseLong(in);
            } catch (NumberFormatException e) {
                return ConfigValidationEnum.INCORRECT_FORMAT;
            }

            if (i < min || i > max) return ConfigValidationEnum.OUT_OF_RANGE;

            return ConfigValidationEnum.VALID;
        }
    }

    public static class FloatValue extends ConfigTypes {
        private float value;
        private float min;
        private float max;

        public FloatValue(String key, float defaultValue, float min, float max, BiConsumer<String, ConfigTypes> manager) {
            super(key, manager);
            value = defaultValue;
            this.min = min;
            this.max = max;
        }

        @Override
        public ConfigValidationEnum validate(String in) {
            float i;
            try {
                i = Float.parseFloat(in);
            } catch (NumberFormatException e) {
                return ConfigValidationEnum.INCORRECT_FORMAT;
            }

            if (i < min || i > max) return ConfigValidationEnum.OUT_OF_RANGE;

            return ConfigValidationEnum.VALID;
        }

        @Override
        public void load(String in) {
            value = Float.parseFloat(in);
        }

        @Override
        public String save() {
            return Float.toString(value);
        }

        public Float get() {
            return value;
        }
    }

    public static class BooleanValue extends ConfigTypes {

        private boolean value;

        public BooleanValue(String key, boolean defaultValue, BiConsumer<String, ConfigTypes> manager) {
            super(key, manager);
            value = defaultValue;
        }

        @Override
        public ConfigValidationEnum validate(String in) {
            if (in.toLowerCase().equals("false") || in.toLowerCase().equals("true")) return ConfigValidationEnum.VALID;
            return ConfigValidationEnum.INCORRECT_FORMAT;
        }

        @Override
        public void load(String in) {
            value = Boolean.parseBoolean(in);
        }

        @Override
        public String save() {
            return Boolean.toString(value);
        }

        public Boolean get() {
            return value;
        }

    }

    public static class ValidatedStringValue extends ConfigTypes {
        private String value;
        private List<String> allowedStrings;


        public ValidatedStringValue(String key, String defaultValue, String[] allowedStrings, BiConsumer<String, ConfigTypes> manager) {
            super(key, manager);
            value = defaultValue;
            this.allowedStrings = Arrays.asList(allowedStrings);
        }

        @Override
        public ConfigValidationEnum validate(String in) {
            if (allowedStrings.contains(in)) return ConfigValidationEnum.VALID;
            return ConfigValidationEnum.OUT_OF_RANGE;
        }


        @Override
        public void load(String in) {
            value = in;
        }


        @Override
        public String save() {
            return value;
        }

        public String get() {
            return value;
        }
    }

    public static class FreeStringValue extends ConfigTypes {
        private String value;

        public FreeStringValue(String key, String defaultValue, BiConsumer<String, ConfigTypes> manager) {
            super(key, manager);
            value = defaultValue;
        }

        @Override
        public ConfigValidationEnum validate(String in) {
            return ConfigValidationEnum.VALID;
        }

        @Override
        public void load(String in) {
            value = in;
        }

        @Override
        public String save() {
            return value;
        }

        public String get() {
            return value;
        }
    }

    public static class StringArrayValue extends ConfigTypes {
        private String[] value;

        public StringArrayValue(String key, String[] defaultValue, BiConsumer<String, ConfigTypes> manager) {
            super(key, manager);
            value = defaultValue;
        }

        @Override
        public ConfigValidationEnum validate(String in) {
            if (in.length() == 0) return ConfigValidationEnum.OUT_OF_RANGE;
            String[] values = in.split(",");
            if (values.length == 0) return ConfigValidationEnum.INCORRECT_FORMAT;
            return ConfigValidationEnum.VALID;
        }

        @Override
        public void load(String in) {
            value = in.split(",");
        }

        @Override
        public String save() {
            return String.join(",", value);
        }

        public String[] get() {
            return value;
        }
    }
}
