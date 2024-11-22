package tech.zhiwei.frostmetal.modules.mydata.constant;

/**
 * mydata 业务常量
 *
 * @author LIEN
 * @since 2024/11/9
 */
public interface MdConstant {
    // ------------------------------ 字段常量 ------------------------------
    /**
     * 编号长度
     */
    int MAX_CODE_LENGTH = 64;

    /**
     * 名称长度
     */
    int MAX_NAME_LENGTH = 64;

    /**
     * URI 长度
     */
    int MAX_URI_LENGTH = 1024;

    /**
     * 描述 长度
     */
    int MAX_DESC_LENGTH = 1024;

    // ------------------------------ API常量 ------------------------------
    /**
     * 请求发送数据的方式：form
     */
    String API_REQUEST_BODY_TYPE_JSON = "json";
    /**
     * 请求发送数据的方式：body
     */
    String API_REQUEST_BODY_TYPE_FORM = "form";

    /**
     * 接口的数据模式：1-单个
     */
    int API_DATA_MODE_SINGLE = 1;
    /**
     * 接口的数据模式：2-多个
     */
    int API_DATA_MODE_LIST = 2;

    // ------------------------------ 流水线任务常量 ------------------------------

    /**
     * 任务类型：从API获取数据
     */
    String TASK_TYPE_API_GET_DATA = "API_GET_DATA";
    /**
     * 任务类型：向API发送数据
     */
    String TASK_TYPE_API_SEND_DATA = "API_SEND_DATA";
    /**
     * 任务类型：解析webhook数据
     */
    String TASK_TYPE_WEBHOOK_GET_DATA = "WEBHOOK_GET_DATA";
    /**
     * 任务类型：从API获取参数
     */
    String TASK_TYPE_API_GET_VAR = "API_GET_VAR";
    /**
     * 任务类型：保存数据到数仓
     */
    String TASK_TYPE_SAVE_DATA = "SAVE_DATA";
    /**
     * 任务类型：从数仓查询数据
     */
    String TASK_TYPE_QUERY_DATA = "QUERY_DATA";
    /**
     * 任务类型：过滤数据
     */
    String TASK_TYPE_FILTER_DATA = "FILTER_DATA";
    /**
     * 任务类型：处理数据
     */
    String TASK_TYPE_OPERATE_DATA = "OPERATE_DATA";
    /**
     * 任务类型：写入Excel文件
     */
    String TASK_TYPE_WRITE_EXCEL = "WRITE_EXCEL";
    /**
     * 任务类型：发送邮件
     */
    String TASK_TYPE_SEND_EMAIL = "SEND_EMAIL";

    /**
     * 任务配置中的常量key：字段映射
     */
    String TASK_CONFIG_KEY_FIELD_MAPPING = "FIELD_MAPPING";

    /**
     * 字段映射的根目录
     */
    String FIELD_MAPPING_ROOT = "/";

    // ------------------------------ 调度任务常量 ------------------------------

    /**
     * 调度任务执行次数：无限次
     */
    int JOB_REPEAT_FOREVER = -1;

    /**
     * 调度任务执行次数：仅1次
     */
    int JOB_REPEAT_ONCE = 1;

    /**
     * 调度任务的数据标识：流水线id
     */
    String JOB_DATA_KEY_PIPELINE_ID = "PIPELINE_ID";

    /**
     * Job上下文共享数据的标识：业务数据
     */
    String JOB_DATA_KEY_BIZ_DATA = "BIZ_DATA";

    /**
     * Job上下文共享数据的标识：API 消费数据
     */
    String JOB_DATA_KEY_API_CONSUME_DATA = "API_CONSUME_DATA";
}
