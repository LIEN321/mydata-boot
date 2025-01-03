package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;
import java.util.List;


/**
 * 流水线分组 VO
 *
 * @author LIEN
 * @since 2024/11/13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线分组")
public class PipelineGroupVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = -6672401461690606928L;

    @Schema(description = "所属项目")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long projectId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "分组描述")
    private String groupDesc;

    @Schema(description = "分组里的流水线列表")
    private List<PipelineVO> pipelines;
}
