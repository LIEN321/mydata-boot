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
 * 应用接口 entity
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_api", autoResultMap = true)
public class AppApi extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 2438675416943172825L;

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
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<Map<String, Object>> reqHeaders;

    /**
     * 请求参数
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<Map<String, Object>> reqParams;

    /**
     * 请求体类型
     */
    private String reqBodyType;

    /**
     * 请求体，form格式
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<Map<String, Object>> reqBodyForm;

    /**
     * 请求体，raw格式
     */
    private String reqBodyRaw;

    /**
     * 响应示例
     */
    private String respExample;

    /**
     * 数据结构模式，1-对象、2-集合
     */
    private Integer dataMode;
}