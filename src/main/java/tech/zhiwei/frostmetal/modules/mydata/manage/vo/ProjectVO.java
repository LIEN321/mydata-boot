package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;


/**
 * 项目 VO
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Schema(description = "项目")
public class ProjectVO extends BaseVO {
    @Schema(description = "项目编号")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目描述")
    private String projectDesc;

}
