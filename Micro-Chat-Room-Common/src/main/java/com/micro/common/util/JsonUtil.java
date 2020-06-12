package com.micro.common.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-10-12 14:01
 */
@Slf4j
public class JsonUtil {

    public static String toJson(Object o) {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonGenerator generator = new JsonFactory().createGenerator(stringWriter);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.writeValue(generator, o);
            generator.close();
            return stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static  <T> T fromJson(String json, Class<T> tClass) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static  <T> T fromJson(String json, JavaType javaType) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JavaType createJavaType(Class<?> clazz, Class<?>... elementClass) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper.getTypeFactory().constructParametricType(clazz, elementClass);
    }

    public static String getRootValueByKey(String json, String key) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            return rootNode.get(key).asText();
        } catch (Exception e) {
            log.error("getRootValueByKey error:" + e.getMessage());
            return null;
        }
    }
}
