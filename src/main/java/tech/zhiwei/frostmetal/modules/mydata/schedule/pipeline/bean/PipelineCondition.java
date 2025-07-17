package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import lombok.Data;

/**
 * 流水线条件
 *
 * @author LIEN
 * @since 2025/7/14
 */
@Data
public class PipelineCondition {
    /**
     * 条件key
     */
    private String key;

    /**
     * 条件操作符
     */
    private String op;

    /**
     * 条件值
     */
    private Object value;
}
