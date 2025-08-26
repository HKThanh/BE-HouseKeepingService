package iuh.house_keeping_service_be.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

@Converter
public class StringArrayConverter implements AttributeConverter<String[], String> {

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }
        return String.join(",", attribute);
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new String[0];
        }
        return dbData.split(",");
    }
}