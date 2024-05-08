package org.springblade.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.mydata.manage.base.TenantEntity;

/**
 * 业务数据概况
 *
 * @author LIEN
 * @since 2024/4/26
 */
@Data
@TableName(value = "md_biz_data", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class BizData extends TenantEntity {
    private static final long serialVersionUID = 8011683780015121905L;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 环境id
     */
    private Long envId;

    /**
     * 数据项id
     */
    private Long dataId;

    /**
     * 数据量
     */
    private Long dataCount;
}
