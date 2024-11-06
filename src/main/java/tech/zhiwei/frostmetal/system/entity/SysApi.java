package tech.zhiwei.frostmetal.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.BaseEntity;
import tech.zhiwei.frostmetal.core.constant.SysConstant;

import java.io.Serial;

/**
 * 系统接口 Entity
 *
 * @author LIEN
 * @since 2024/9/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_SYS_API)
public class SysApi extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -3579179025048244297L;

    /**
     * 所属菜单id
     */
    private Long menuId;

    /**
     * 接口编号
     */
    private String code;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口地址
     */
    private String path;

    /**
     * 请求方式
     */
    private String method;

    /**
     * 备注
     */
    private String remark;
}
