package tech.zhiwei.frostmetal.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

/**
 * 业务实体 DTO
 *
 * @author LIEN
 * @since 2024/9/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "业务实体参数")
public class DevEntityDTO extends BaseDTO {
    @Schema(description = "实体编号")
    private String code;

    @Schema(description = "实体名称")
    private String name;

    @Schema(description = "后端包名")
    private String packageName;

    @Schema(description = "数据库表名")
    private String tableName;

    @Schema(description = "实体备注")
    private String remark;
}
