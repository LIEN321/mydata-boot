package tech.zhiwei.frostmetal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

import java.util.Date;

/**
 * 系统租户 DTO
 *
 * @author LIEN
 * @since 2024/11/02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统租户")
public class TenantDTO extends BaseDTO {
    @Schema(description = "租户id")
    private String tenantId;

    @Schema(description = "租户编号")
    private String tenantCode;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "到期时间")
    private Date expireTime;

    @Schema(description = "联系人姓名")
    private String contactsName;

    @Schema(description = "联系人电话")
    private String contactsPhone;

    @Schema(description = "联系人地址")
    private String contactsAddress;

}
