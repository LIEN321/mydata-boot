package tech.zhiwei.frostmetal.dev.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

/**
 * 业务实体VO
 *
 * @author LIEN
 * @since 2024/9/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "业务实体")
public class DevEntityVO extends BaseVO {
    @Schema(description = "实体编号")
    private String code;

    @Schema(description = "实体姓名")
    private String name;

    @Schema(description = "后端包名")
    private String packageName;

    @Schema(description = "数据库表名")
    private String tableName;

    @Schema(description = "实体备注")
    private String remark;

    @Schema(description = "继承父类的模式")
    private String extendMode;

    @Schema(description = "最后一次生成的作者名")
    private String authName;

    @Schema(description = "最后一次生成java代码的路径")
    private String javaCodePath;

    @Schema(description = "最后一次生成ui代码的路径")
    private String uiCodePath;
}
