package tech.zhiwei.frostmetal.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

/**
 * 业务实体属性 DTO
 *
 * @author LIEN
 * @since 2024/10/08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "业务实体属性参数")
public class DevEntityPropertyDTO extends BaseDTO {
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
