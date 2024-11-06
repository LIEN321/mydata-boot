package tech.zhiwei.frostmetal.dev.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;

/**
 * 业务实体属性VO
 *
 * @author LIEN
 * @since 2024/10/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "业务实体属性")
public class DevEntityPropertyVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = -7464348442685725916L;

    @Schema(description = "属性编号")
    private String code;

    @Schema(description = "属性名称")
    private String name;

    @Schema(description = "属性类型")
    private String type;

    @Schema(description = "是否显示在列表")
    private Boolean isList;

    @Schema(description = "是否作为查询条件")
    private Boolean isSearch;

    @Schema(description = "查询类型")
    private String searchType;

    @Schema(description = "是否显示在表单")
    private Boolean isForm;

    @Schema(description = "表单组件")
    private String formComponent;

    @Schema(description = "是否必填")
    private Boolean isRequired;

}
