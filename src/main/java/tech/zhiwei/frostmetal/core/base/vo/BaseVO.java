package tech.zhiwei.frostmetal.core.base.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基础VO对象
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
public abstract class BaseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4745887887687023294L;

    @Schema(description = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "业务状态")
    private Integer status;
}
