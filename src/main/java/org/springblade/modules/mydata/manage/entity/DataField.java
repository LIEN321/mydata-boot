package org.springblade.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.mydata.manage.base.TenantEntity;

/**
 * 标准数据项字段实体类
 *
 * @author LIEN
 * @since 2022-07-09
 */
@Data
@TableName("md_data_field")
@EqualsAndHashCode(callSuper = true)
public class DataField extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属数据项
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
     * 是否标识，0-不是、1-是
     */
    private Integer isId;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 字段显示模式：0-不显示、1-显示
     */
    private Integer displayMode;
}
