package org.springblade.modules.mydata.job.bean;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.common.constant.MdConstant;
import org.springblade.modules.mydata.data.BizDataFilter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 包含执行接口的 任务对象
 *
 * @author LIEN
 * @since 2022/07/14
 */
@Data
@EqualsAndHashCode(of = "id")
public class TaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    // ----- 任务相关信息 -----
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 所属环境
     */
    private Long envId;

    /**
     * 任务周期
     */
    private String taskPeriod;

    /**
     * 操作类型
     *
     * @see MdConstant#DATA_PRODUCER
     * @see MdConstant#DATA_CONSUMER
     */
    private Integer opType;

    /**
     * 待执行次数，默认Integer.MAX_VALUE，0 结束，正整数 待执行数
     */
    private int times = MdConstant.TASK_JOB_DEFAULT_TIMES;

    // ----- 接口相关信息 -----

    /**
     * 接口method
     */
    private String apiMethod;

    /**
     * 接口地址
     */
    private String apiUrl;

    /**
     * 接口类型：json
     */
    private String dataType;

    /**
     * 接口请求Header
     */
    private Map<String, String> reqHeaders;

    /**
     * 接口请求参数
     */
    private Map<String, Object> reqParams;

    /**
     * 接口字段与变量名的映射
     */
    private Map<String, String> fieldVarMapping;

    // ----- 数据相关信息 -----

    /**
     * 字段层级前缀
     */
    private String apiFieldPrefix;

    /**
     * 字段映射配置
     */
    private LinkedHashMap<String, String> fieldMapping;

    /**
     * 所属租户
     */
    private String tenantId;

    /**
     * 所属数据项id
     */
    private Long dataId;

    /**
     * 所属数据编号
     */
    private String dataCode;

    /**
     * 唯一标识字段编号
     */
    private String idFieldCode;

    /**
     * 数据的过滤条件
     */
    private List<BizDataFilter> dataFilters;

    // ----- 定时任务相关信息 -----

    /**
     * 下一次执行时间
     */
    private Date nextRunTime = new Date();

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 执行时间
     */
    private Date lastRunTime;

    /**
     * 最后成功时间
     */
    private Date lastSuccessTime;

    /**
     * 日志记录
     */
    private StringBuffer log = new StringBuffer();

    /**
     * 执行结果，0-失败，1-成功
     */
    private int executeResult = MdConstant.TASK_RESULT_FAILED;

    /**
     * 任务失败次数
     */
    private int failCount = 0;

    /**
     * 是否为订阅任务：0-不订阅，1-订阅
     */
    private Integer isSubscribed;

    /**
     * 接口返回的数据
     */
    private List<Map> produceDataList;

    /**
     * 待消费的数据
     */
    private List<Map> consumeDataList;

    /**
     * 被过滤的无效数据
     */
    private List<Map> filteredDataList;

    /**
     * 所属项目id
     */
    private Long projectId;

    /**
     * 分批启用状态：0-不启用，1-启用
     */
    private boolean isBatch;

    /**
     * 分批间隔（秒）
     */
    private Integer batchInterval;

    /**
     * 分批参数
     */
    private List<TaskBatchParam> batchParams;

    /**
     * 分批数量
     */
    private Integer batchSize;

    /**
     * 配置映射的数据字段的类型
     */
    private Map<String, String> mappingFieldType;

    /**
     * 提供数据模式，默认1，1-API、2-接收推送
     */
    private Integer produceMode;

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
     * 任务是否失败
     */
    private boolean isFailed = false;

    /**
     * 任务创建者id
     */
    private Long createUser;

    /**
     * 新增数据总量
     */
    private int insertCount;

    /**
     * 更新数据总理
     */
    private int updateCount;

    /**
     * 消费数据总量
     */
    private int consumeCount;

    /**
     * 接收到的数据
     */
    private String acceptedData;

    /**
     * 追加日志
     */
    public void appendLog(String log, Object... params) {
        String template = DateUtil.format(new Date(), DatePattern.NORM_DATETIME_MS_PATTERN) + " - " + log;
        String newLog = StrUtil.format(template, params);
        this.log.append(newLog).append("\n");
    }
}
