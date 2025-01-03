package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户的集成配置 DTO
 *
 * @author LIEN
 * @since 2024/12/23
 */
@Data
@EqualsAndHashCode
@Schema(description = "用户的集成配置")
public class UserConfigDTO {
    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "最新管理的项目id")
    private Long latestProjectId;
}
