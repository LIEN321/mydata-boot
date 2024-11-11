package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;


/**
 * 应用 VO
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Schema(description = "应用")
public class AppVO extends BaseVO {
    @Schema(description = "应用编号")
    private String appCode;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "访问地址")
    private String appUrl;

    @Schema(description = "接口前缀地址")
    private String apiPrefix;

    @Schema(description = "应用描述")
    private String appDesc;

    @Schema(description = "接口数量")
    private Integer apiCount;

}
