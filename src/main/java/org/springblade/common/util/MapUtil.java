package org.springblade.common.util;

import cn.hutool.core.collection.CollUtil;

import java.util.Map;

/**
 * Map 工具类
 *
 * @author LIEN
 * @since 2022/8/11
 */
public class MapUtil extends cn.hutool.core.map.MapUtil {
    /**
     * 合并map
     *
     * @param baseMap  基础map
     * @param unionMap 并入baseMap的map
     * @return 合并后的map
     */
    public static <K, V> Map<K, V> union(Map<K, V> baseMap, Map<K, V> unionMap) {
        if (CollUtil.isEmpty(baseMap)) {
            baseMap = cn.hutool.core.map.MapUtil.newHashMap(true);
        }
        if (CollUtil.isEmpty(unionMap)) {
            unionMap = cn.hutool.core.map.MapUtil.newHashMap(true);
        }

        baseMap.putAll(unionMap);

        return baseMap;
    }
}
