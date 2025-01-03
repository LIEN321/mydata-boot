package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;


/**
 * 标准数据字段 VO
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Schema(description = "标准数据字段")
public class DataFieldVO extends BaseVO {
    @Schema(description = "所属数据")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataId;

    @Schema(description = "字段编号")
    private String fieldCode;

    @Schema(description = "字段名称")
    private String fieldName;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "字段默认值")
    private String defaultValue;

    @Schema(description = "是否标识")
    private Integer isId;

    @Schema(description = "显示模式")
    private Integer displayMode;

}
