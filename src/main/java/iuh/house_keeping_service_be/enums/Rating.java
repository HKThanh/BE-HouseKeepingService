package iuh.house_keeping_service_be.enums;

public enum Rating {
    LOWEST, LOW, MEDIUM, HIGH, HIGHEST;

    public static Rating fromAverage(Double average) {
        if (average == null) {
            return LOWEST;
        }

        double value = average;
        if (Double.isNaN(value) || value <= 0) {
            return LOWEST;
        }
        if (value < 2.0) {
            return LOWEST;
        }
        if (value < 3.0) {
            return LOW;
        }
        if (value < 4.0) {
            return MEDIUM;
        }
        if (value < 4.5) {
            return HIGH;
        }
        return HIGHEST;
    }
}
