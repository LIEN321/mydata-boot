package tech.zhiwei.frostmetal.core.base.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 层级结构DTO
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TreeDTO extends BaseDTO {
    private Long parentId;
}
