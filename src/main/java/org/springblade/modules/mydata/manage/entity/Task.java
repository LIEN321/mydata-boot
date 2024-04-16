package org.springblade.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.common.constant.MdConstant;
import org.springblade.modules.mydata.manage.base.TenantEntity;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 集成任务实体类
 *
 * @author LIEN
 * @since 2022-07-11
 */
@Data
@TableName(value = "md_task", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class Task extends TenantEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 所属环境
     */
    private Long envId;

    /**
     * 所属应用接口
     */
    private Long apiId;

    /**
     * 接口完整地址
     */
    private String apiUrl;

    /**
     * 操作类型
     *
     * @see MdConstant#DATA_PRODUCER
     * @see MdConstant#DATA_CONSUMER
     */
    private Integer opType;

    /**
     * 接口请求类型
     */
    private String apiMethod;

    /**
     * 所属数据
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
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
    @TableField(typeHandler = FastjsonTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private LinkedHashMap<String, String> fieldMapping;

    /**
     * 运行状态
     *
     * @see MdConstant#TASK_STATUS_RUNNING  运行
     * @see MdConstant#TASK_STATUS_FAILED   异常
     * @see MdConstant#TASK_STATUS_STOPPED  停止
     */
    private Integer taskStatus;

    /**
     * 接口数据类型：JSON
     */
    private String dataType;

    /**
     * 数据编号
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String dataCode;

    /**
     * 数据主键字段编号
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String idFieldCode;

    /**
     * 是否为订阅任务：0-不订阅，1-订阅
     */
    private Integer isSubscribed;

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
     * 数据的过滤条件
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<Map<String, Object>> dataFilter;

    /**
     * 最后执行时间
     */
    private Date lastRunTime;

    /**
     * 最后成功时间
     */
    private Date lastSuccessTime;

    /**
     * 接口字段与变量名的映射
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private Map<String, String> fieldVarMapping;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 所属项目id
     */
    private Long projectId;

    /**
     * 跨环境任务的对应目标环境id
     */
    private Long refEnvId;

    /**
     * 跨环境任务的对应操作类型
     *
     * @see MdConstant#DATA_PRODUCER
     * @see MdConstant#DATA_CONSUMER
     */
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
     *
     * @see MdConstant#TASK_PRODUCE_MODE_API
     * @see MdConstant#TASK_PRODUCE_MODE_PUSH
     */
    private Integer produceMode;

    /**
     * 提供数据的 认证方式
     *
     * @see MdConstant#TASK_AUTH_TYPE_NONE
     * @see MdConstant#TASK_AUTH_TYPE_API_KEY
     * @see MdConstant#TASK_AUTH_TYPE_BASIC
     * @see MdConstant#TASK_AUTH_TYPE_HMAC
     */
    private Integer authType;

    /**
     * 提供数据的 认证参数
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private Map<String, String> authParams;

    /**
     * 消费数据模式，默认1，1-API、2-发邮件
     *
     * @see MdConstant#TASK_CONSUME_MODE_API
     * @see MdConstant#TASK_CONSUME_MODE_EMAIL
     */
    private Integer consumeMode;

    /**
     * 消费数据模式的收件人邮件
     */
    private String consumeEmail;

    /**
     * 跳过特殊情况
     * 0-不跳过
     * 1-跳过相同数据异常
     */
    private Integer skipError;

    /**
     * 单条记录消费模式，1-对象、2-集合
     */
    private Integer singleMode;

    /**
     * 订阅任务id
     */
    private Long subscribeTaskId;

    /**
     * 下次执行时间
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Date nextRunTime;
}
