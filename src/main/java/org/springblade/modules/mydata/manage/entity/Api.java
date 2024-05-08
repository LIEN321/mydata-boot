package org.springblade.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.modules.mydata.manage.base.TenantEntity;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 应用接口实体类
 *
 * @author LIEN
 * @since 2022-07-08
 */
@Data
@TableName(value = "md_api", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class Api extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 操作类型，1-提供数据、2-消费数据
     */
    private Integer opType;

    /**
     * 接口请求类型
     */
    private String apiMethod;

    /**
     * 接口相对路径
     */
    private String apiUri;

    /**
     * 接口数据类型：JSON
     */
    private String dataType;

    /**
     * 接口请求Header
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private LinkedHashMap<String, String> reqHeaders;

    /**
     * 接口请求参数
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private LinkedHashMap<String, String> reqParams;

    /**
     * 同步到任务的时间
     */
    private Date syncTaskTime;

    /**
     * 所属应用id
     */
    private Long appId;

    /**
     * 字段层级前缀
     */
    private String fieldPrefix;

    /**
     * 请求体
     */
    private String reqBody;
}
