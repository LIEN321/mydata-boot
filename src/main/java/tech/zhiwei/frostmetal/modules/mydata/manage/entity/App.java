package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;


/**
 * 应用 entity
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_app")
public class App extends TenantEntity {
    /**
     * 应用编号
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 访问地址
     */
    private String appUrl;

    /**
     * 接口前缀地址
     */
    private String apiPrefix;

    /**
     * 应用描述
     */
    private String appDesc;

    /**
     * 接口数量
     */
    private Integer apiCount;

}