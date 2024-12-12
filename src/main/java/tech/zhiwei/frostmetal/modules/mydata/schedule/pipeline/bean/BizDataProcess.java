package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import lombok.Data;

/**
 * 数据处理方式
 *
 * @author LIEN
 * @since 2024/12/12
 */
@Data
public class BizDataProcess {
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
