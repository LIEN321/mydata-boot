package tech.zhiwei.frostmetal.core.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 主键实体类
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
public abstract class IdEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 8452167728677176121L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "记录的唯一id")
    private Long id;
}
