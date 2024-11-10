package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;


/**
 * 项目 DTO
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "项目")
public class ProjectDTO extends BaseDTO {
    @Schema(description = "项目编号")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目描述")
    private String projectDesc;

}
