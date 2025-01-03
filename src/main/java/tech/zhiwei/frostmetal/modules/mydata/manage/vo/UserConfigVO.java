package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;

/**
 * 用户的集成配置 VO
 *
 * @author LIEN
 * @since 2024/12/23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户的集成配置")
public class UserConfigVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = -2884578541250056358L;
//    @Schema(description = "用户id")
//    @JsonSerialize(using = ToStringSerializer.class)
//    private Long userId;

    @Schema(description = "最新管理的项目id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long latestProjectId;

}
