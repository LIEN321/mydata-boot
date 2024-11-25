package tech.zhiwei.frostmetal.modules.mydata.data;

import lombok.Data;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.tool.lang.StringUtil;

import java.io.Serializable;

/**
 * 任务的数据过滤条件 封装类
 *
 * @author LIEN
 * @since 2024/11/25
 */
@Data
public class BizDataFilter implements Serializable {
    private static final long serialVersionUID = 3175245476047659373L;

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
     * @see MdConstant#TASK_FILTER_TYPE_VALUE
     * @see MdConstant#TASK_FILTER_TYPE_FIELD
     */
    private Object type;

    @Override
    public String toString() {
        return StringUtil.format("{} {} {}", key, op, value);
    }
}
