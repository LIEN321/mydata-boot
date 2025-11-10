package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流水线变量 DTO
 *
 * @author LIEN
 * @since 2025/10/10
 */
@Data
@Schema(description = "流水线变量")
public class PipelineVarDTO {
    @Schema(description = "所属流水线")
    private Long pipelineId;

    @Schema(description = "变量编号")
    private String varCode;

    @Schema(description = "变量值")
    private String varValue;

    @Schema(description = "变量值类型")
    private String varType;

    @Schema(description = "变量描述")
    private String varDesc;

    @Schema(description = "是否隐藏")
    private Integer isHide;

}
