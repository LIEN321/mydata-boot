package org.springblade.modules.mydata.manage.vo;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 集成任务视图实体类
 *
 * @author LIEN
 * @since 2022-07-11
 */
@Data
@EqualsAndHashCode
public class TaskVO {
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
     * 接口完整地址
     */
    private String apiUrl;

    /**
     * 操作类型，1-提供数据、2-消费数据
     */
    private Integer opType;

    /**
     * 接口请求类型
     */
    private String apiMethod;

    /**
     * 所属数据
     */
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long dataId;

    /**
     * 任务周期
     */
    private String taskPeriod;

    /**
     * 字段层级前缀
     */
    private String apiFieldPrefix;

    /**
     * 字段映射
     */
    private Map<String, String> fieldMapping;

    /**
     * 运行状态：0-停止，1-运行
     */
    private Integer taskStatus;

    /**
     * 接口数据类型：JSON
     */
    private String dataType;

    /**
     * 数据项编号
     */
    private String dataCode;

    /**
     * 数据项名称
     */
    private String dataName;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 环境名称
     */
    private String envName;

    /**
     * 是否为订阅任务：0-不订阅，1-订阅
     */
    private Integer isSubscribed;

    /**
     * 数据的过滤条件
     */
    private List<Map<String, Object>> dataFilter;

    /**
     * 最后执行时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date lastRunTime;

    /**
     * 最后成功时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date lastSuccessTime;

    /**
     * 接口字段与变量名的映射
     */
    private List<Map<String, String>> fieldVarMapping;

    /**
     * 所属项目id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long projectId;

    /**
     * 所属项目名称
     */
    private String projectName;

    /**
     * 跨环境任务的对应目标环境id
     */
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long refEnvId;

    /**
     * 环境名称
     */
    private String refEnvName;

    /**
     * 跨环境任务的对应操作类型
     */
    @JsonSerialize(nullsUsing = NullSerializer.class)
    private Integer refOpType;

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
    @TableField(typeHandler = FastjsonTypeHandler.class)
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
     * 跳过特殊情况
     */
    private Integer skipError;

    /**
     * 单条记录消费模式，1-对象、2-集合
     */
    @JsonSerialize(nullsUsing = NullSerializer.class)
    private Integer dataMode;

    /**
     * 订阅任务id
     */
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long subscribeTaskId;

    /**
     * 订阅任务名称
     */
    private String subscribeTaskName;

    /**
     * 下次执行时间
     */
    private Date nextRunTime;

    /**
     * 所属应用
     */
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 数据处理配置
     */
    private Map<String, Map<String, String>> dataProcess;
}
