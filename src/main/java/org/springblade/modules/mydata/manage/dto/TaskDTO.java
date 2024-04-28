package org.springblade.modules.mydata.manage.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 集成任务数据传输对象实体类
 *
 * @author LIEN
 * @since 2022-07-11
 */
@Data
@EqualsAndHashCode
public class TaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 所属环境
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long envId;

    /**
     * 所属应用接口
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long apiId;

    /**
     * 所属数据
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataId;

    /**
     * 任务周期
     */
    private String taskPeriod;

    /**
     * 字段层级前缀
     */
    //    private String apiFieldPrefix;

    /**
     * 字段映射
     */
    private Map<String, String> fieldMapping;

    /**
     * 是否为订阅任务：0-不订阅，1-订阅
     */
    private Integer isSubscribed;

    /**
     * 数据的过滤条件
     */
    private List<Map<String, Object>> dataFilter;

    /**
     * 接口字段与变量名的映射
     */
    private Map<String, String> fieldVarMapping;

    /**
     * 所属项目id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long projectId;

    /**
     * 跨环境任务的对应目标环境id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long refEnvId;

    /**
     * 分批启用状态：0-不启用，1-启用
     */
    private Integer batchStatus;

    /**
     * 分批间隔（秒）
     */
    private Integer batchInterval;

    /**
     * 分批参数
     */
    private List<Map<String, String>> batchParams;

    /**
     * 分批数量
     */
    private Integer batchSize;

    /**
     * 提供数据模式，默认1，1-API、2-接收推送
     */
    private Integer produceMode;

    /**
     * 提供数据的 认证方式
     */
    private Integer authType;

    /**
     * 提供数据的 认证参数
     */
    private Map<String, String> authParams;

    /**
     * 消费数据模式，默认1，1-API、2-发邮件
     */
    private Integer consumeMode;

    /**
     * 消费数据模式的收件人邮件
     */
    private String consumeEmail;

    /**
     * 操作类型
     */
    private Integer opType;

    /**
     * 跳过特殊情况
     */
    private Integer skipError;

    /**
     * 字段层级前缀
     */
    private String apiFieldPrefix;

    /**
     * 单条记录消费模式，1-对象、2-集合
     */
    private Integer dataMode;

    /**
     * 订阅任务id
     */
    private Long subscribeTaskId;
}
