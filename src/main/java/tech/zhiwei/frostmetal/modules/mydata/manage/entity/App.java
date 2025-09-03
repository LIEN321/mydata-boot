package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;
import java.util.List;
import java.util.Map;


/**
 * 应用 entity
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_app", autoResultMap = true)
public class App extends TenantEntity {
    @Serial
    private static final long serialVersionUID = -228704270689519953L;
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

    /**
     * App全局Header
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<Map<String, Object>> reqHeaders;

    /**
     * 认证类型，null-无、api_key、cookie
     */
    private String authType;

    /**
     * 认证配置
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> authConfig;
}