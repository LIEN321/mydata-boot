package tech.zhiwei.tool.map;

import java.util.HashMap;
import java.util.Map;

/**
 * Map工具类
 *
 * @author LIEN
 * @since 2024/10/5
 */
public class MapUtil extends cn.hutool.core.map.MapUtil {
    /**
     * 将map2并入map1
     *
     * @param map1 被合并的map
     * @param map2 并入的map
     * @param <K>  key
     * @param <V>  value
     * @return 合并后的新map
     */
    public static <K, V> Map<K, V> union(Map<K, V> map1, Map<K, V> map2) {
        if (map1 == null && map2 == null) {
            return empty();
        }

        if (map1 == null) {
            return map2;
        } else if (map2 == null) {
            return map1;
        }

        Map<K, V> result = new HashMap<>(map1);
        result.putAll(map2);
        return result;
    }
}
