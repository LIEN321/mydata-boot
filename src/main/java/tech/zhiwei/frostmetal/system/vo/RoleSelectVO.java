package tech.zhiwei.frostmetal.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色 下拉选项VO
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Data
@Schema(description = "角色 下拉选项VO")
public class RoleSelectVO {
    @Schema(description = "角色名称")
    private String label;

    @Schema(description = "角色id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long value;
}
