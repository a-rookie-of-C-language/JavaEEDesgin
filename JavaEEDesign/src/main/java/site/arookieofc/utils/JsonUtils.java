package site.arookieofc.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
    
    public static String toJson(Object obj) throws Exception {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return OBJECT_MAPPER.readValue(json, clazz);
    }
}