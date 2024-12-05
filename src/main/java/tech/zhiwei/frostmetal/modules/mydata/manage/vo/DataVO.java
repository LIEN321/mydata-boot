package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;
import java.util.List;


/**
 * 标准数据 VO
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标准数据")
public class DataVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = -488148377797940325L;

    @Schema(description = "所属项目id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long projectId;

    @Schema(description = "所属项目名称")
    private String projectName;

    @Schema(description = "数据编号")
    private String dataCode;

    @Schema(description = "数据名称")
    private String dataName;

    @Schema(description = "业务数据")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataCount;

    @Schema(description = "字段列表")
    private List<DataFieldVO> dataFields;
}
