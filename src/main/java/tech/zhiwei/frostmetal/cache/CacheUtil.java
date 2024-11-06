package tech.zhiwei.frostmetal.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.concurrent.Callable;

/**
 * 缓存操作 工具类
 *
 * @author LIEN
 * @since 2024/9/12
 */
public class CacheUtil {
    private static final CacheManager cacheManager;

    static {
        cacheManager = SpringUtil.getBean(CacheManager.class);
    }

    /**
     * 获取缓存对象
     *
     * @param name 缓存名称
     * @return Cache对象
     */
    public static Cache getCache(String name) {
        return cacheManager.getCache(name);
    }

    /**
     * 存入缓存对象
     *
     * @param name  缓存名称
     * @param key   缓存key
     * @param value 缓存对象
     */
    public static void put(String name, Object key, Object value) {
        getCache(name).put(key, value);
    }

    /**
     * 存入缓存对象
     *
     * @param name      缓存名称
     * @param keyPrefix 缓存key前缀
     * @param key       缓存key
     * @param value     缓存对象
     */
    public static void put(String name, String keyPrefix, Object key, Object value) {
        put(name, keyPrefix.concat(String.valueOf(key)), value);
    }

    /**
     * 获取缓存对象
     *
     * @param name 缓存名称
     * @param key  缓存key
     * @return 缓存对象
     */
    @Nullable
    public static Object get(String name, String key) {
        if (ObjectUtil.hasEmpty(name, key)) {
            return null;
        }
        Cache.ValueWrapper valueWrapper = getCache(name).get(key);
        if (valueWrapper != null) {
            return valueWrapper.get();
        }
        return null;
    }

    @Nullable
    public static Object get(String name, String keyPrefix, Object key) {
        return get(name, keyPrefix.concat(String.valueOf((key))));
    }

    @Nullable
    public static <T> T get(String name, String keyPrefix, Object key, Callable<T> valueLoader) {
        if (ObjectUtil.hasEmpty(name, keyPrefix, key)) {
            return null;
        }
        try {
            Cache.ValueWrapper valueWrapper = getCache(name).get(keyPrefix.concat(String.valueOf(key)));
            Object value = null;
            if (valueWrapper == null) {
                if (valueLoader != null) {
                    T call = valueLoader.call();
                    if (ObjectUtil.isNotEmpty(call)) {
                        getCache(name).put(keyPrefix.concat(String.valueOf(key)), call);
                        value = call;
                    }
                }
            } else {
                value = valueWrapper.get();
            }
            return (T) value;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 删除缓存
     *
     * @param name 缓存名称
     * @param key  缓存key
     */
    public static void remove(String name, String key) {
        getCache(name).evict(key);
    }

    /**
     * 删除缓存
     *
     * @param name      缓存名称
     * @param keyPrefix 缓存key前缀
     * @param key       缓存key值
     */
    public static void remove(String name, String keyPrefix, Object key) {
        getCache(name).evict(keyPrefix.concat(String.valueOf(key)));
    }
}
