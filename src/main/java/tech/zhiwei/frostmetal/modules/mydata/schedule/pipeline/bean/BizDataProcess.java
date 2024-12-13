package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import lombok.Data;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;

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

    /**
     * 条件类型
     *
     * @see MyDataConstant#TASK_FILTER_TYPE_VALUE
     * @see MyDataConstant#TASK_FILTER_TYPE_FIELD
     */
    private Object type;
}
