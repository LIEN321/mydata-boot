package org.springblade.modules.mydata.manage.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标准数据项字段视图实体类
 *
 * @author LIEN
 * @since 2022-07-09
 */
@Data
@EqualsAndHashCode
public class DataFieldVO {
    private static final long serialVersionUID = 1L;

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
}
