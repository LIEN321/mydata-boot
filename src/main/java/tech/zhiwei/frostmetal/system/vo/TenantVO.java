package tech.zhiwei.frostmetal.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;
import java.util.Date;

/**
 * 系统租户 VO
 *
 * @author LIEN
 * @since 2024/11/02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统租户")
public class TenantVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = 5067791232919891424L;
    @Schema(description = "租户id")
    private String tenantId;

    @Schema(description = "租户编号")
    private String tenantCode;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    @Schema(description = "联系人姓名")
    private String contactsName;

    @Schema(description = "联系人电话")
    private String contactsPhone;

    @Schema(description = "联系人地址")
    private String contactsAddress;

}
