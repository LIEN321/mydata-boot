package tech.zhiwei.frostmetal.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.BaseEntity;
import tech.zhiwei.frostmetal.core.constant.SysConstant;

/**
 * 系统参数 Entity
 *
 * @author LIEN
 * @since 2024/9/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_SYS_PARAMETER)
public class SysParameter extends BaseEntity {
    /**
     * 参数编号
     */
    private String code;

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数值
     */
    private String value;

    /**
     * 参数备注
     */
    private String remark;
}
