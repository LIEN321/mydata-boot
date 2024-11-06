package tech.zhiwei.frostmetal.dev.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;

/**
 * 业务实体
 *
 * @author LIEN
 * @since 2024/9/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_DEV_ENTITY)
public class DevEntity extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 2241766367763549818L;
    /**
     * 实体编号
     */
    private String code;

    /**
     * 实体名称
     */
    private String name;

    /**
     * 后端包名
     */
    private String packageName;

    /**
     * 数据库表名
     */
    private String tableName;

    /**
     * 实体备注
     */
    private String remark;

    /**
     * 继承父类的模式
     *
     * @see tech.zhiwei.frostmetal.core.constant.DevConstant#EXTEND_MODE_ID
     * @see tech.zhiwei.frostmetal.core.constant.DevConstant#EXTEND_MODE_BASE
     * @see tech.zhiwei.frostmetal.core.constant.DevConstant#EXTEND_MODE_TREE
     */
    private String extendMode;

    /**
     * 最后一次生成的作者名
     */
    private String authName;

    /**
     * 最后一次生成java代码的路径
     */
    private String javaCodePath;

    /**
     * 最后一次生成ui代码的路径
     */
    private String uiCodePath;

}
