package tech.zhiwei.frostmetal.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 生成业务实体代码 DTO
 *
 * @author LIEN
 * @since 2024/10/7
 */
@Data
@Schema(description = "生成业务实体代码参数")
public class GenerateCodeDTO {
    @Schema(description = "所选实体id数组")
    private Long[] ids;

    @Schema(description = "最后一次生成的作者名")
    private String authName;

    @Schema(description = "最后一次生成java代码的路径")
    private String javaCodePath;

    @Schema(description = "最后一次生成ui代码的路径")
    private String uiCodePath;
}
