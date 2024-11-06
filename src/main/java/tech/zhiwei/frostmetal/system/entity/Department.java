package tech.zhiwei.frostmetal.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.core.tenant.entity.TreeTenantEntity;

import java.io.Serial;

/**
 * 机构部门 Entity
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_SYS_DEPARTMENT)
public class Department extends TreeTenantEntity {
    @Serial
    private static final long serialVersionUID = 6004768660028044314L;
    /**
     * 机构名称
     */
    private String name;
    /**
     * 机构类型
     */
    private Integer type;
    /**
     * 机构排序
     */
    private Integer sort;
    /**
     * 机构备注
     */
    private String remark;
}
