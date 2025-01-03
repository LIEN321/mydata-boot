package tech.zhiwei.frostmetal.dev.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.IdEntity;
import tech.zhiwei.frostmetal.core.constant.SysConstant;

import java.io.Serial;


/**
 * 业务实体属性 entity
 *
 * @author LIEN
 * @since 2024/10/07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_DEV_ENTITY_PROPERTY)
public class DevEntityProperty extends IdEntity {
    @Serial
    private static final long serialVersionUID = -3965721947513261820L;
    /**
     * 所属实体id
     */
    private Long entityId;

    /**
     * 属性编号
     */
    private String code;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 属性类型
     */
    private String type;

    /**
     * 是否显示在列表
     */
    private Boolean isList;

    /**
     * 是否作为查询条件
     */
    private Boolean isSearch;

    /**
     * 查询类型
     */
    private String searchType;

    /**
     * 是否显示在表单
     */
    private Boolean isForm;

    /**
     * 表单组件
     */
    private String formComponent;

    /**
     * 是否必填
     */
    private Boolean isRequired;

}