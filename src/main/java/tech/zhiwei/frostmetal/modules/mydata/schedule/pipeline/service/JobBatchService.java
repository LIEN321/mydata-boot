package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Component;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.util.NumberUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务分批参数处理类
 *
 * @author LIEN
 * @since 2024/12/3
 */
@Component
public class JobBatchService {
    private static final String OP_INC = "inc";

    /**
     * 将分批参数 转为请求参数
     *
     * @param batchParams 分批参数
     * @return 请求参数
     */
    public Map<String, String> parseToMap(List<Map<String, Object>> batchParams) {
        if (CollUtil.isEmpty(batchParams)) {
            return null;
        }

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (CollectionUtil.isNotEmpty(batchParams)) {
            batchParams.forEach(item -> {
                map.put((String) item.get("code"), StringUtil.toStringOrEmpty(item.get("value")));
            });
        }
        return map;
    }

    /**
     * 递增分批参数
     *
     * @param batchParams 分批参数
     */
    public void incBatchParam(List<Map<String, Object>> batchParams) {
        if (CollectionUtil.isEmpty(batchParams)) {
            return;
        }

        for (Map<String, Object> param : batchParams) {
            if (OP_INC.equals(param.getOrDefault("op", "fix"))) {
                Integer value = NumberUtil.parseInt(param.get("value").toString());
                Integer step = NumberUtil.parseInt(param.get("step").toString());
                param.put("value", value + step);
            }
        }
    }
}
