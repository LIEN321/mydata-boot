package tech.zhiwei.frostmetal.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;

/**
 * 用户 Entity
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_SYS_USER)
public class User extends TenantEntity {
    @Serial
    private static final long serialVersionUID = -4513614983931467633L;
    /**
     * 编号
     */
    private String code;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 所属部门id
     */
    private Long departmentId;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 登录账号
     */
    private String loginName;

    /**
     * 登录密码
     */
    private String loginPassword;

    /**
     * 密码加盐
     */
    private String salt;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 密码复杂度
     */
    private Integer passwordStrength;
}