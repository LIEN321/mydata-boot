package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;


/**
 * 流水线分组 DTO
 *
 * @author LIEN
 * @since 2024/11/13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线分组")
public class PipelineGroupDTO extends BaseDTO {
    @Schema(description = "所属项目")
    private Long projectId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "分组描述")
    private String groupDesc;

}
