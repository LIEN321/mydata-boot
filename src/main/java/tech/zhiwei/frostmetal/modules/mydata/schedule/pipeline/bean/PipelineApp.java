package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import lombok.Data;
import tech.zhiwei.tool.map.MapUtil;

import java.util.Map;

/**
 * 流水线内的应用信息
 *
 * @author LIEN
 * @since 2025/9/13
 */
@Data
public class PipelineApp {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 应用编号
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 接口前缀地址
     */
    private String apiPrefix;

    /**
     * App全局Header
     */
    private Map<String, String> reqHeaders = MapUtil.newHashMap();

    /**
     * App全局Query param
     */
    private Map<String, Object> queryParams = MapUtil.newHashMap();
}
