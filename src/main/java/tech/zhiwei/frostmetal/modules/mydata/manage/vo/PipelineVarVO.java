package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;

/**
 * 流水线变量 VO
 *
 * @author LIEN
 * @since 2025/10/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线变量")
public class PipelineVarVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = -2834222095558437204L;
    
    @Schema(description = "所属流水线")
    @JsonSerialize(using = ToStringSerializer.class)
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
