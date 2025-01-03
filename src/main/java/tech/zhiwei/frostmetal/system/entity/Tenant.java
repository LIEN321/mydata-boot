package tech.zhiwei.frostmetal.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.BaseEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 系统租户 entity
 *
 * @author LIEN
 * @since 2024/11/02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_tenant")
public class Tenant extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 7459013348406798261L;
    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 租户编号
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 到期时间
     */
    private Date expireTime;

    /**
     * 联系人姓名
     */
    private String contactsName;

    /**
     * 联系人电话
     */
    private String contactsPhone;

    /**
     * 联系人地址
     */
    private String contactsAddress;

}