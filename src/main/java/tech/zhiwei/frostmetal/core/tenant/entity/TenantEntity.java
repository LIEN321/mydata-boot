package tech.zhiwei.frostmetal.core.tenant.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.BaseEntity;

import java.io.Serial;

/**
 * 租户实体类
 *
 * @author LIEN
 * @since 2024/11/2
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TenantEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -7099979920321229907L;
    /**
     * 所属租户
     */
    private String tenantId;
}
