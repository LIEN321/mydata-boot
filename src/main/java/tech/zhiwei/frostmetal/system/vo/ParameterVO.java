package tech.zhiwei.frostmetal.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

/**
 * 系统参数VO
 *
 * @author LIEN
 * @since 2024/9/1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统参数")
public class ParameterVO extends BaseVO {
    @Schema(description = "系统参数编号")
    private String code;

    @Schema(description = "系统参数名称")
    private String name;

    @Schema(description = "系统参数值")
    private String value;

    @Schema(description = "系统参数备注")
    private String remark;
}
