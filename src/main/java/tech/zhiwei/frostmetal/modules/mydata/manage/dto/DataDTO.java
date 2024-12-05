package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

import java.util.List;


/**
 * 标准数据 DTO
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标准数据")
public class DataDTO extends BaseDTO {
    @Schema(description = "所属项目")
    private Long projectId;

    @Schema(description = "数据编号")
    private String dataCode;

    @Schema(description = "数据名称")
    private String dataName;

    @Schema(description = "字段列表")
    private List<DataFieldDTO> dataFields;

}
