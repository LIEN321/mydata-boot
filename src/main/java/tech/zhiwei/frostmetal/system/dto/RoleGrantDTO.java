package tech.zhiwei.frostmetal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色分配权限 DTO
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Data
@Schema(description = "角色分配授权参数")
public class RoleGrantDTO {
    @Schema(description = "角色id集合")
    private List<Long> roleIds;
    
    @Schema(description = "菜单id集合")
    private List<Long> menuIds;
}
