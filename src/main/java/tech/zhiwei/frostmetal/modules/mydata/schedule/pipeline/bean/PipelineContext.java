package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import cn.hutool.core.map.SafeConcurrentHashMap;
import com.yomahub.liteflow.context.ContextBean;
import com.yomahub.liteflow.exception.NullParamException;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Map;

/**
 * MyData流水线上下文，内置ConcurrentHashMap
 *
 * @author LIEN
 * @since 2026/1/6
 */
@ContextBean("context")
public class PipelineContext {
    private final SafeConcurrentHashMap<String, Object> context = new SafeConcurrentHashMap<>();

    /**
     * 获取上下文map
     *
     * @return 上下文map
     */
    public Map<String, Object> getMap() {
        return context;
    }

    /**
     * 向流水线存入参数
     *
     * @param key   参数名
     * @param value 参数值
     */
    public void put(String key, Object value) {
        if (ObjectUtil.isNull(value)) {
            throw new NullParamException("MyData: context value can not be null");
        }
        context.put(key, value);
    }

    /**
     * 从流水线获取参数值
     *
     * @param key 参数名
     * @return 参数值
     */
    public Object get(String key) {
        return context.get(key);
    }

    /**
     * 从流水线删除参数
     *
     * @param key 参数名
     * @return 被删除的参数值
     */
    public Object remove(String key) {
        return context.remove(key);
    }

    /**
     * 批量存入参数
     *
     * @param m 新的map
     */
    public void putAll(Map<String, Object> m) {
        context.putAll(m);
    }
}
