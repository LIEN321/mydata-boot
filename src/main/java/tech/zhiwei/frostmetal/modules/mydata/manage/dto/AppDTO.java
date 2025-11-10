package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

import java.util.List;
import java.util.Map;


/**
 * 应用 DTO
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用")
public class AppDTO extends BaseDTO {
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

    @Schema(description = "请求Header")
    private List<Map<String, Object>> reqHeaders;

    @Schema(description = "认证类型")
    private String authType;

    @Schema(description = "认证配置")
    private Map<String, Object> authConfig;

    @Schema(description = "响应配置")
    private Map<String, Object> respConfig;
}
