package tech.zhiwei.frostmetal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

/**
 * 系统参数 DTO
 *
 * @author LIEN
 * @since 2024/9/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统参数")
public class ParameterDTO extends BaseDTO {
    @Schema(description = "参数编号")
    private String code;

    @Schema(description = "参数名称")
    private String name;

    @Schema(description = "参数值")
    private String value;

    @Schema(description = "参数备注")
    private String remark;
}
