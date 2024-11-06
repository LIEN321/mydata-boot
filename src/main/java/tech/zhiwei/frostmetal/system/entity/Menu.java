package tech.zhiwei.frostmetal.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.TreeEntity;
import tech.zhiwei.frostmetal.core.constant.SysConstant;

import java.io.Serial;

/**
 * 菜单 Entity
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_SYS_MENU)
public class Menu extends TreeEntity {
    @Serial
    private static final long serialVersionUID = 495038349578628990L;
    /**
     * 菜单编号
     */
    private String code;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 路由地址
     */
    private String path;
    /**
     * 菜单排序
     */
    private Integer sort;
    /**
     * 菜单类型
     */
    private Integer type;
    /**
     * 菜单备注
     */
    private String remark;
}
