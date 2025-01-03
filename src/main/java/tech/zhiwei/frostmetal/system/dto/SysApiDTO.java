package tech.zhiwei.frostmetal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

/**
 * 系统接口 Entity
 *
 * @author LIEN
 * @since 2024/9/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统接口参数")
public class SysApiDTO extends BaseDTO {
    @Schema(description = "所属菜单id")
    private Long menuId;

    @Schema(description = "接口编号")
    private String code;

    @Schema(description = "接口名称")
    private String name;

    @Schema(description = "接口地址")
    private String path;

    @Schema(description = "请求方式")
    private String method;

    @Schema(description = "备注")
    private String remark;
}
