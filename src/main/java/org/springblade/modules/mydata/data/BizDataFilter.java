package org.springblade.modules.mydata.data;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springblade.common.constant.MdConstant;

import java.io.Serializable;

/**
 * 任务的数据过滤条件 封装类
 *
 * @author LIEN
 * @since 2023/2/7
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
        return StrUtil.format("{} {} {}", key, op, value);
    }
}
