package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import lombok.Data;
import tech.zhiwei.tool.map.MapUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 流水线内的应用信息
 *
 * @author LIEN
 * @since 2025/9/13
 */
@Data
public class PipelineApp implements Serializable {
    @Serial
    private static final long serialVersionUID = 5248056790657601249L;
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
