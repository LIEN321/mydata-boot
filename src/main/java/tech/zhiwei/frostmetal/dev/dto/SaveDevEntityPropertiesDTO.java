package tech.zhiwei.frostmetal.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

import java.util.List;

/**
 * 保存业务实体属性 DTO
 *
 * @author LIEN
 * @since 2024/10/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "保存业务实体属性参数")
public class SaveDevEntityPropertiesDTO extends BaseDTO {
    @Schema(description = "业务实体id")
    private Long id;

    @Schema(description = "继承父类的模式")
    private String extendMode;

    @Schema(description = "属性列表")
    private List<DevEntityPropertyDTO> properties;
}
