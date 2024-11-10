package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.IdEntity;

import java.io.Serial;


/**
 * 标准数据字段 entity
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_data_field")
public class DataField extends IdEntity {
    @Serial
    private static final long serialVersionUID = 5337664077611878753L;

    /**
     * 所属数据
     */
    private Long dataId;

    /**
     * 字段编号
     */
    private String fieldCode;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 字段默认值
     */
    private String defaultValue;

    /**
     * 是否标识
     */
    private Boolean isId;

    /**
     * 显示模式
     */
    private Boolean displayMode;

}