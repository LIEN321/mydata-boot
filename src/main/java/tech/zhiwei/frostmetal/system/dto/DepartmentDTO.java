package tech.zhiwei.frostmetal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.TreeDTO;

/**
 * 机构部门 DTO
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "机构部门参数")
public class DepartmentDTO extends TreeDTO {
    @Schema(description = "机构名称")
    private String name;

    @Schema(description = "机构类型")
    private Integer type;

    @Schema(description = "机构排序")
    private Integer sort;
    
    @Schema(description = "机构备注")
    private String remark;
}
