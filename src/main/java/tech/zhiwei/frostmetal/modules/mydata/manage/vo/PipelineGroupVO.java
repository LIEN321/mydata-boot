package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;


/**
 * 流水线分组 VO
 *
 * @author LIEN
 * @since 2024/11/13
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Schema(description = "流水线分组")
public class PipelineGroupVO extends BaseVO {
    @Schema(description = "所属项目")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long projectId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "分组描述")
    private String groupDesc;

}
