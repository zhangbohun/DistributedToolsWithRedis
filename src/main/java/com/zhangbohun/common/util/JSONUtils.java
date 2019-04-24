package com.zhangbohun.common.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import static com.zhangbohun.common.util.JSONUtils.SerializerFeature.*;

/**
 * @author zhangbohun
 * Create Date 2019/04/16 14:56
 * Modify Date 2019/04/17 16:59
 */
public class JSONUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setTimeZone(TimeZone.getDefault());

        //未知属性不处理
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //支持j非标准的注释和单引号
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);

        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJSONString(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("生成 JSON 失败!", e);
        }
    }

    public static String toJSONStringWithoutPretty(Object object) {
        try {
            OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, false);
            String jsonString = OBJECT_MAPPER.writeValueAsString(object);
            OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
            return jsonString;
        } catch (Exception e) {
            throw new RuntimeException("生成 JSON 失败!", e);
        }
    }

    public static String toJSONString(Object object, SerializerFeature... features) {
        try {
            String jsonString = OBJECT_MAPPER.setSerializerFactory(OBJECT_MAPPER.getSerializerFactory()
                                                                                .withSerializerModifier(
                                                                                    new CustomSerializerFeatureSupportSerializerModifier(
                                                                                        features)))
                                             .writerWithDefaultPrettyPrinter().writeValueAsString(object);
            //重置
            OBJECT_MAPPER.setSerializerFactory(BeanSerializerFactory.instance);
            return jsonString;
        } catch (Exception e) {
            throw new RuntimeException("生成 JSON 失败!", e);
        }
    }

    public static String toJSONStringWithoutPretty(Object object, SerializerFeature... features) {
        try {
            String jsonString = OBJECT_MAPPER.setSerializerFactory(OBJECT_MAPPER.getSerializerFactory()
                                                                                .withSerializerModifier(
                                                                                    new CustomSerializerFeatureSupportSerializerModifier(
                                                                                        features)))
                                             .writeValueAsString(object);
            //重置
            OBJECT_MAPPER.setSerializerFactory(BeanSerializerFactory.instance);
            return jsonString;
        } catch (Exception e) {
            throw new RuntimeException("生成 JSON 失败!", e);
        }
    }

    public static <T> T parseObject(String jsonString, Class<T> t) {
        ObjectReader objectReader = OBJECT_MAPPER.readerFor(t);
        try {
            return objectReader.readValue(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败! " + jsonString, e);
        }
    }

    public static <T> T parseObject(JsonNode node, Class<T> objClazz) {
        ObjectReader objectReader = OBJECT_MAPPER.readerFor(objClazz);
        try {
            return objectReader.readValue(node);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败! " + node.toString(), e);
        }
    }

    public static <T> List<T> parseList(String jsonString, Class<T> elementClazz) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, elementClazz);
        try {
            return OBJECT_MAPPER.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败!\n" + jsonString, e);
        }
    }

    public enum SerializerFeature {
        WriteNullListAsEmpty,
        // List 字段如果为 null，输出为 []，而不是 null
        WriteNullNumberAsZero,
        // 数值字段如果为 null，输出为 0，而不是 null
        WriteNullBooleanAsFalse,
        // Boolean 字段如果为 null，输出为 false，而不是 null
        WriteNullStringAsEmpty;// 字符类型字段如果为 null，输出为 ""，而不是 null

        public final int mask;

        SerializerFeature() {
            mask = (1 << ordinal());
        }
    }

    final private static class CustomSerializerFeatureSupportSerializerModifier extends BeanSerializerModifier {
        final private JsonSerializer<Object> nullBooleanJsonSerializer;
        final private JsonSerializer<Object> nullNumberJsonSerializer;
        final private JsonSerializer<Object> nullListJsonSerializer;
        final private JsonSerializer<Object> nullStringJsonSerializer;

        CustomSerializerFeatureSupportSerializerModifier(SerializerFeature... features) {
            int config = 0;
            for (SerializerFeature feature : features) {
                config |= feature.mask;
            }
            nullBooleanJsonSerializer = (config & WriteNullBooleanAsFalse.mask) != 0 ? new NullBooleanSerializer()
                : null;
            nullNumberJsonSerializer = (config & WriteNullNumberAsZero.mask) != 0 ? new NullNumberSerializer() : null;
            nullListJsonSerializer = (config & WriteNullListAsEmpty.mask) != 0 ? new NullListJsonSerializer() : null;
            nullStringJsonSerializer = (config & WriteNullStringAsEmpty.mask) != 0 ? new NullStringSerializer() : null;
        }

        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {
            for (BeanPropertyWriter writer : beanProperties) {
                final JavaType javaType = writer.getType();
                final Class<?> rawClass = javaType.getRawClass();
                if (javaType.isArrayType() || javaType.isCollectionLikeType()) {
                    writer.assignNullSerializer(nullListJsonSerializer);
                } else if (Number.class.isAssignableFrom(rawClass) && rawClass.getName().startsWith("java.lang")) {
                    writer.assignNullSerializer(nullNumberJsonSerializer);
                } else if (Boolean.class.equals(rawClass)) {
                    writer.assignNullSerializer(nullBooleanJsonSerializer);
                } else if (String.class.equals(rawClass)) {
                    writer.assignNullSerializer(nullStringJsonSerializer);
                }
            }
            return beanProperties;
        }

        private static class NullListJsonSerializer extends JsonSerializer<Object> {
            @Override
            public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeStartArray();
                jgen.writeEndArray();
            }
        }

        private static class NullNumberSerializer extends JsonSerializer<Object> {
            @Override
            public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeNumber(0);
            }
        }

        private static class NullBooleanSerializer extends JsonSerializer<Object> {
            @Override
            public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeBoolean(false);
            }
        }

        private static class NullStringSerializer extends JsonSerializer<Object> {
            @Override
            public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeString("");
            }
        }
    }
}
