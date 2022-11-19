package com.yesheng.util.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * JSON工具类,提供序列化、反序列化、类型解析
 * <p>
 * 序列化特性：
 * 1、忽略null值，null值属性不会被序列化
 * 2、根据字段进行序列化及反序列化，可以忽略get和set方法
 *
 * @author Max
 */
public final class JsonUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private final static ObjectMapper OBJECT_MAPPER;

    public final static String ERROR_MESSAGE = "Exception thrown while parsing ObjectMapper.";

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        //禁止序列化空值
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);

        // 禁止遇到空原始类型时抛出异常，用默认值代替。
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

        // 禁止遇到未知（新）属性时报错，支持兼容扩展
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    private JsonUtils() {
    }

    /**
     * 将json的byte数组解析为JsonNode
     *
     * @param data 需要jie的byte数组
     * @return 一个 {@link JsonNode}，如果解析出现异常则返回null.
     */
    public static JsonNode parseJsonNode(byte[] data) {
        return parseJsonNode(data, true);
    }

    /**
     * 将json的byte数组解析为JsonNode
     *
     * @param data             需要jie的byte数组
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @return 一个 {@link JsonNode}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static JsonNode parseJsonNode(byte[] data, boolean swallowException) {
        try {
            return OBJECT_MAPPER.readTree(data);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 异常处理，是否吞掉异常，使用参数控制
     *
     * @param swallowException 是否吞掉异常
     * @param e                Json相关编解码异常
     */
    private static void swallowException(boolean swallowException, Exception e) {
        if (!swallowException) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将String类型json解析为JsonNode
     *
     * @param content 需要解析的内容
     * @return 一个 {@link JsonNode}，如果出现异常则返回null.
     */
    public static JsonNode parseJsonNode(String content) {
        return parseJsonNode(content, true);
    }

    /**
     * 将String类型json解析为JsonNode.
     *
     * @param content          需要解析的内容
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @return 一个 {@link JsonNode}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static JsonNode parseJsonNode(String content, boolean swallowException) {
        try {
            return OBJECT_MAPPER.readTree(content);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将对象序列化为json字符串
     *
     * @param object 需要序列化的对象
     * @return 一个json规范的String，如果出现异常则返回null.
     */
    public static String toJsonString(Object object) {
        return toJsonString(object, true);
    }

    /**
     * 将对象序列化为json字符串
     *
     * @param object           需要序列化的对象
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @return 一个json规范的String，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static String toJsonString(Object object, boolean swallowException) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将byte类型的json数据解析为{@code List}集合， 可以指定集合元素类型
     *
     * @param data 需要解析的内容
     * @param type 需要解析的对象类型
     * @param <T>  集合元素的类型
     * @return 一个 {@link List}，如果出现异常则返回null.
     */
    public static <T> List<T> parseList(byte[] data, Class<T> type) {
        return parseList(data, type, true);
    }

    /**
     * 将byte类型的json数据解析为{@code List}集合， 可以指定集合元素类型
     *
     * @param data             需要解析的内容
     * @param type             需要解析的对象类型
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              集合元素的类型
     * @return 一个 {@link List}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> List<T> parseList(byte[] data, Class<T> type, boolean swallowException) {
        try {
            CollectionType collectionType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, type);
            return OBJECT_MAPPER.readValue(data, collectionType);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将String类型的json数据解析为{@code List}集合， 可以指定集合元素类型
     *
     * @param content 需要解析的内容
     * @param type    需要解析的对象类型
     * @param <T>     集合元素的类型
     * @return 一个 {@link List}，如果出现异常则返回null.
     */
    public static <T> List<T> parseList(String content, Class<T> type) {
        return parseList(content, type, true);
    }

    /**
     * 将String类型的json数据解析为{@code List}集合， 可以指定集合元素类型
     *
     * @param content          需要解析的内容
     * @param type             需要解析的对象类型
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              集合元素的类型
     * @return 一个 {@link List}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> List<T> parseList(String content, Class<T> type, boolean swallowException) {
        try {
            CollectionType collectionType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, type);
            return OBJECT_MAPPER.readValue(content, collectionType);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 从InputStream中获取数据解析为{@code List}集合， 可以指定集合元素类型
     *
     * @param stream 需要解析的流对象
     * @param type   需要解析的对象类型
     * @param <T>    集合元素的类型
     * @return 一个 {@link List}，如果出现异常则返回null.
     */
    public static <T> List<T> parseList(InputStream stream, Class<T> type) {
        return parseList(stream, type, true);
    }

    /**
     * 从InputStream中获取数据解析为{@code List}集合， 可以指定集合元素类型
     *
     * @param stream           需要解析的流对象
     * @param type             需要解析的对象类型
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              集合元素的类型
     * @return 一个 {@link List}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> List<T> parseList(InputStream stream, Class<T> type, boolean swallowException) {
        try {
            CollectionType collectionType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, type);
            return OBJECT_MAPPER.readValue(stream, collectionType);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将byte类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param data      需要解析的内容
     * @param valueType 需要解析Map集合Value的类型
     * @param <T>       Map集合Value的类型
     * @return 一个 {@link Map}，如果解析异常则返回null.
     */
    public static <T> Map<String, T> parseMap(byte[] data, Class<T> valueType) {
        return parseMap(data, String.class, valueType, true);
    }

    /**
     * 将byte类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param data             需要解析的内容
     * @param valueType        需要解析Map集合Value的类型
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              Map集合Value的类型
     * @return 一个 {@link Map}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> Map<String, T> parseMap(byte[] data, Class<T> valueType, boolean swallowException) {
        return parseMap(data, String.class, valueType, swallowException);
    }

    /**
     * 将String类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param content   需要解析的内容
     * @param valueType 需要解析Map集合Value的类型
     * @param <V>       Map集合Value的类型
     * @return 一个 {@link Map}，如果解析异常则返回null.
     */
    public static <V> Map<String, V> parseMap(String content, Class<V> valueType) {
        return parseMap(content, valueType, true);
    }

    /**
     * 将String类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param content          需要解析的内容
     * @param valueType        需要解析Map集合Value的类型
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <V>              Map集合Value的类型
     * @return 一个 {@link Map}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <V> Map<String, V> parseMap(String content, Class<V> valueType, boolean swallowException) {
        return parseMap(content, String.class, valueType, swallowException);
    }

    /**
     * 将byte类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param stream    需要解析的流
     * @param valueType 需要解析Map集合Value的类型
     * @param <V>       Map集合Value的类型
     * @return 一个 {@link Map}，如果解析异常则返回null.
     */
    public static <V> Map<String, V> parseMap(InputStream stream, Class<V> valueType) {
        return parseMap(stream, valueType, true);
    }

    /**
     * 将byte类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param stream           需要解析的流
     * @param valueType        需要解析Map集合Value的类型
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <V>              Map集合Value的类型
     * @return 一个 {@link Map}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <V> Map<String, V> parseMap(InputStream stream, Class<V> valueType, boolean swallowException) {
        return parseMap(stream, String.class, valueType, swallowException);
    }


    /**
     * 将byte类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param data      需要解析的内容
     * @param keyType   需要解析Map集合Key的类型
     * @param valueType 需要解析Map集合Value的类型
     * @param <K>       Map集合Key的类型
     * @param <V>       Map集合Value的类型
     * @return 一个 {@link Map}，如果解析异常则返回null.
     */
    public static <K, V> Map<K, V> parseMap(byte[] data, Class<K> keyType, Class<V> valueType) {
        return parseMap(data, keyType, valueType, true);
    }

    /**
     * 将byte类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param data             需要解析的内容
     * @param keyType          需要解析Map集合Key的类型
     * @param valueType        需要解析Map集合Value的类型
     * @param <K>              Map集合Key的类型
     * @param <V>              Map集合Value的类型
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @return 一个 {@link Map}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <K, V> Map<K, V> parseMap(byte[] data, Class<K> keyType, Class<V> valueType, boolean swallowException) {
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valueType);
        try {
            return OBJECT_MAPPER.readValue(data, mapType);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将String类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param content   需要解析的内容
     * @param keyType   需要解析Map集合Key的类型
     * @param valueType 需要解析Map集合Value的类型
     * @param <K>       Map集合Key的类型
     * @param <V>       Map集合Value的类型
     * @return 一个 {@link Map}，如果解析异常则返回null.
     */
    public static <K, V> Map<K, V> parseMap(String content, Class<K> keyType, Class<V> valueType) {
        return parseMap(content, keyType, valueType, true);
    }

    /**
     * 将String类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param content          需要解析的内容
     * @param keyType          需要解析Map集合Key的类型
     * @param valueType        需要解析Map集合Value的类型
     * @param <K>              Map集合Key的类型
     * @param <V>              Map集合Value的类型
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @return 一个 {@link Map}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <K, V> Map<K, V> parseMap(String content, Class<K> keyType, Class<V> valueType, boolean swallowException) {
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valueType);
        try {
            return OBJECT_MAPPER.readValue(content, mapType);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将byte类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param stream    需要解析的流
     * @param keyType   需要解析Map集合Key的类型
     * @param valueType 需要解析Map集合Value的类型
     * @param <K>       Map集合Key的类型
     * @param <V>       Map集合Value的类型
     * @return 一个 {@link Map}，如果解析异常则返回null.
     */
    public static <K, V> Map<K, V> parseMap(InputStream stream, Class<K> keyType, Class<V> valueType) {
        return parseMap(stream, keyType, valueType, true);
    }

    /**
     * 将byte类型的json数据解析为{@code Map}集合， 可以指定集合元素类型
     *
     * @param stream           需要解析的流
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param keyType          需要解析Map集合Key的类型
     * @param valueType        需要解析Map集合Value的类型
     * @param <K>              Map集合Key的类型
     * @param <V>              Map集合Value的类型
     * @return 一个 {@link Map}，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <K, V> Map<K, V> parseMap(InputStream stream, Class<K> keyType, Class<V> valueType, boolean swallowException) {
        MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valueType);
        try {
            return OBJECT_MAPPER.readValue(stream, mapType);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将任何对象类型的解析为Json规范的byte数组
     *
     * @param object 需要转化的对象
     * @return 一个java数组，如果解析异常则返回null.
     */
    public static byte[] parseByteArray(Object object) {
        return parseByteArray(object, true);
    }

    /**
     * 将任何对象类型的解析为Json规范的byte数组
     *
     * @param object           需要转化的对象
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @return 一个java数组，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static byte[] parseByteArray(Object object, boolean swallowException) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 从输入流中读取数据解析为Java对象
     *
     * @param stream 需要解析的输入流
     * @param type   需要解析的类型Class
     * @param <T>    需要解析的类型
     * @return 一个Java对象由 {@param type}指定，如果解析异常则返回null.
     */
    public static <T> T parseObject(InputStream stream, Class<T> type) {
        return parseObject(stream, type, true);
    }

    /**
     * 从输入流中读取数据解析为Java对象
     *
     * @param stream           需要解析的输入流
     * @param type             需要解析的类型Class
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              需要解析的类型
     * @return 一个Java对象由 {@param type}指定，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> T parseObject(InputStream stream, Class<T> type, boolean swallowException) {
        try {
            return OBJECT_MAPPER.readValue(stream, type);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 从输入流中读取数据解析为Java对象
     *
     * @param stream 需要解析的输入流
     * @param type   需要解析的类型TypeReference
     * @param <T>    需要解析的类型
     * @return 一个Java对象由 {@param type}指定，如果解析异常则返回null.
     */
    public static <T> T parseObject(InputStream stream, TypeReference<T> type) {
        return parseObject(stream, type, true);
    }

    /**
     * 从输入流中读取数据解析为Java对象
     *
     * @param stream           需要解析的输入流
     * @param type             需要解析的类型TypeReference
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              需要解析的类型
     * @return 一个Java对象由 {@param type}指定，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> T parseObject(InputStream stream, TypeReference<T> type, boolean swallowException) {
        try {
            return OBJECT_MAPPER.readValue(stream, type);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将String类型的json数据解析为Java对象
     *
     * @param content 需要解析的内容
     * @param type    需要解析的类型Class
     * @param <T>     需要解析的类型
     * @return 一个Java对象由 {@param type}指定，如果解析异常则返回null.
     */
    public static <T> T parseObject(String content, Class<T> type) {
        return parseObject(content, type, true);
    }

    /**
     * 从输入流中读取数据解析为Java对象
     *
     * @param content          需要解析的内容
     * @param type             需要解析的类型Class
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              需要解析的类型
     * @return 一个Java对象由{@param type}指定，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> T parseObject(String content, Class<T> type, boolean swallowException) {
        try {
            return OBJECT_MAPPER.readValue(content, type);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将Json规范的byte数据解析为Java对象
     *
     * @param content 需要解析的数据
     * @param type    需要解析的类型TypeReference
     * @param <T>     需要解析的类型
     * @return 一个Java对象由 {@param type}指定，如果解析异常则返回null.
     */
    public static <T> T parseObject(String content, TypeReference<T> type) {
        return parseObject(content, type, true);
    }

    /**
     * 将Json规范的byte数据解析为Java对象
     *
     * @param content 需要解析的数据
     * @param type    需要解析的类型TypeReference
     * @param <T>     需要解析的类型
     * @return 一个Java对象由 {@param type}指定，如果解析异常则返回null.
     */
    public static <T> T parseObject(String content, TypeReference<T> type, boolean swallowException) {
        try {
            return OBJECT_MAPPER.readValue(content, type);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }

    /**
     * 将Json规范的byte数据解析为Java对象
     *
     * @param data 需要解析的数据
     * @param type 需要解析的类型Class
     * @param <T>  需要解析的类型
     * @return 一个Java对象由 {@param type}指定，如果解析异常则返回null.
     */
    public static <T> T parseObject(byte[] data, Class<T> type) {
        return parseObject(data, type, true);
    }

    /**
     * 将Json规范的byte数据解析为Java对象
     *
     * @param data             需要解析的数据
     * @param type             需要解析的类型Class
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              需要解析的类型
     * @return 一个Java对象由 {@param type}指定，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> T parseObject(byte[] data, Class<T> type, boolean swallowException) {
        try {
            return OBJECT_MAPPER.readValue(data, type);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }


    /**
     * 将Json规范的byte数据解析为Java对象
     *
     * @param data 需要解析的数据
     * @param type 需要解析的类型Class
     * @param <T>  需要解析的类型
     * @return 一个Java对象由 {@param type}指定，如果解析异常则返回null.
     */
    public static <T> T parseObject(byte[] data, TypeReference<T> type) {
        return parseObject(data, type, true);
    }

    /**
     * 将Json规范的byte数据解析为Java对象
     *
     * @param data             需要解析的数据
     * @param type             需要解析的类型Class
     * @param swallowException 是否吞掉异常，如果是则不抛出异常，否则异常往上抛出
     * @param <T>              需要解析的类型
     * @return 一个Java对象由 {@param type}指定，当解析产生异常而{@param swallowException}为false的时候则会将异常抛出，
     * 客户方需要捕获异常，为true则会吞掉异常，返回null
     */
    public static <T> T parseObject(byte[] data, TypeReference<T> type, boolean swallowException) {
        try {
            return OBJECT_MAPPER.readValue(data, type);
        } catch (Exception e) {
            swallowException(swallowException, e);
            LOGGER.error(ERROR_MESSAGE, e);
        }
        return null;
    }
}
