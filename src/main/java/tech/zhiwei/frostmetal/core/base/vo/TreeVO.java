package tech.zhiwei.frostmetal.core.base.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础TreeVO
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TreeVO extends BaseVO {
    @Schema(description = "父菜单id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
}
