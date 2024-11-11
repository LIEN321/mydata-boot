package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;


/**
 * 应用接口 entity
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_api")
public class AppApi extends TenantEntity {
    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 操作类型
     */
    private Integer opType;

    /**
     * 请求方法
     */
    private String apiMethod;

    /**
     * 接口路径
     */
    private String apiUri;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据层级
     */
    private String fieldPrefix;

    /**
     * 请求Header
     */
    private String reqHeaders;

    /**
     * 请求参数
     */
    private String reqParams;

    /**
     * 请求体
     */
    private String reqBody;

    /**
     * 响应示例
     */
    private String respExample;

}