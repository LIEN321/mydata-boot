package tech.zhiwei.frostmetal.core.base.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 基础Entity
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseEntity extends IdEntity {
    @Serial
    private static final long serialVersionUID = 4442042497230516166L;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createUser;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新人id
     */
    @Schema(description = "更新人id")
    private Long updateUser;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 创建部门id
     */
    @Schema(description = "创建部门id")
    private Long createDepartment;

    /**
     * 更新部门id
     */
    @Schema(description = "更新部门id")
    private Long updateDepartment;

    /**
     * 业务状态
     */
    @Schema(description = "业务状态")
    private Integer status;

    /**
     * 删除标记
     */
    @TableLogic
    @Schema(description = "删除标记")
    private Integer isDeleted;
}
