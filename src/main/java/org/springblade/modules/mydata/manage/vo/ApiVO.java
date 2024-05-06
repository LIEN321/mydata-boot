package org.springblade.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 应用接口视图实体类
 *
 * @author LIEN
 * @since 2022-07-08
 */
@Data
@EqualsAndHashCode
public class ApiVO {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 同步到任务的时间
     */
    private Date syncTaskTime;

    /**
     * 接口请求Header
     */
    private List<Map<String, String>> reqHeaders;

    /**
     * 接口请求参数
     */
    private List<Map<String, String>> reqParams;

    /**
     * 所属应用id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appId;

    /**
     * 应用编号
     */
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 字段层级前缀
     */
    private String fieldPrefix;

    /**
     * 请求体
     */
    private String reqBody;
}
