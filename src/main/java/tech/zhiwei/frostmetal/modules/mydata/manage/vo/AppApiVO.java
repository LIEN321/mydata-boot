package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * 应用接口 VO
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用接口")
public class AppApiVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = -4123748090378512319L;

    @Schema(description = "所属应用")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appId;

    @Schema(description = "所属应用名称")
    private String appName;

    @Schema(description = "接口名称")
    private String apiName;

    @Schema(description = "操作类型")
    private Integer opType;

    @Schema(description = "请求方法")
    private String apiMethod;

    @Schema(description = "接口路径")
    private String apiUri;

    @Schema(description = "数据类型")
    private String dataType;

    @Schema(description = "数据层级")
    private String fieldPrefix;

    @Schema(description = "请求Header")
    private List<Map<String, Object>> reqHeaders;

    @Schema(description = "请求参数")
    private List<Map<String, Object>> reqParams;

    @Schema(description = "请求体类型")
    private String reqBodyType;

    @Schema(description = "请求体，form格式")
    private List<Map<String, Object>> reqBodyForm;

    @Schema(description = "请求体，raw格式")
    private String reqBodyRaw;

    @Schema(description = "响应示例")
    private String respExample;

    @Schema(description = "数据结构模式，1-对象、2-集合")
    private Integer dataMode;
}
