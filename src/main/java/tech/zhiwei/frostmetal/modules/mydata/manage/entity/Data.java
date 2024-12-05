package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;


/**
 * 标准数据 entity
 *
 * @author LIEN
 * @since 2024/11/09
 */
@lombok.Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_data")
public class Data extends TenantEntity {
    @Serial
    private static final long serialVersionUID = -518238127748282817L;
    /**
     * 所属项目
     */
    private Long projectId;

    /**
     * 数据编号
     */
    private String dataCode;

    /**
     * 数据名称
     */
    private String dataName;

    /**
     * 业务数据
     */
    private Long dataCount;

}
