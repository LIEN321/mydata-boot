package tech.zhiwei.frostmetal.modules.mydata.constant;

/**
 * mydata 业务常量
 *
 * @author LIEN
 * @since 2024/11/9
 */
public interface MyDataConstant {
    // 临时目录名
    String TEMP_DIR = "mydata";

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

    /**
     * 数据操作类型：数据提供者
     */
    int DATA_PRODUCER = 1;

    /**
     * 数据操作类型：数据消费者
     */
    int DATA_CONSUMER = 2;

    // ------------------------------ 流水线任务常量 ------------------------------

    /**
     * 任务类型：从API获取数据
     */
    String TASK_TYPE_API_GET_JSON = "API_GET_JSON";
    /**
     * 任务类型：向API发送数据
     */
    String TASK_TYPE_API_SEND_DATA = "API_SEND_DATA";
    /**
     * 任务类型：解析webhook数据
     */
    String TASK_TYPE_WEBHOOK_GET_JSON = "WEBHOOK_GET_JSON";
    /**
     * 任务类型：用Webhook触发流水线
     */
    String TASK_TYPE_TRIGGER_PIPELINE = "TRIGGER_PIPELINE";
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
     * 任务类型：JSON转数据
     */
    String TASK_TYPE_JSON_TO_DATA = "JSON_TO_DATA";
    /**
     * 任务类型：JSON转数据
     */
    String TASK_TYPE_DATA_TO_JSON = "DATA_TO_JSON";
    /**
     * 任务类型：过滤数据
     */
    String TASK_TYPE_FILTER_DATA = "FILTER_DATA";
    /**
     * 任务类型：处理数据
     */
    String TASK_TYPE_PROCESS_DATA = "PROCESS_DATA";
    /**
     * 任务类型：数据写入Excel
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
     * 任务配置中的常量key：业务数据在json中的前缀层级
     */
    String TASK_CONFIG_KEY_FIELD_PREFIX = "FIELD_PREFIX";

    /**
     * 任务配置中的常量key：任务输入变量名
     */
    String TASK_CONFIG_KEY_INPUT = "INPUT";
    /**
     * 任务配置中的常量key：任务输出变量名
     */
    String TASK_CONFIG_KEY_OUTPUT = "OUTPUT";

    /**
     * 任务的定时起始时间的格式
     */
    String TASK_TIME_FORMAT = "HH:mm";

    /**
     * 认证类型，无需认证
     */
    Integer TASK_AUTH_TYPE_NONE = 0;
    /**
     * 认证类型，api key
     */
    Integer TASK_AUTH_TYPE_API_KEY = 1;

    /**
     * ApiKey
     */
    String TASK_AUTH_API_KEY = "ApiKey";

    /**
     * api key 的名称
     */
    String TASK_AUTH_API_KEY_KEY = "key";
    /**
     * api key value 的名称
     */
    String TASK_AUTH_API_KEY_VALUE = "value";

    /**
     * 认证类型，basic auth
     */
    Integer TASK_AUTH_TYPE_BASIC = 2;

    /**
     * BasicAuth
     */
    String TASK_AUTH_BASIC = "BasicAuth";

    /**
     * Basic Auth的username
     */
    String TASK_AUTH_BASIC_USERNAME = "username";
    /**
     * Basic Auth的password
     */
    String TASK_AUTH_BASIC_PASSWORD = "password";
    /**
     * Basic Auth的header
     */
    String TASK_AUTH_BASIC_HEADER = "Authorization";
    /**
     * 认证类型，hmac
     */
    Integer TASK_AUTH_TYPE_HMAC = 3;

    /**
     * HMAC的algorithm
     */
    String TASK_AUTH_HMAC_ALGORITHM = "algorithm";

    /**
     * HMAC的encoding
     */
    String TASK_AUTH_HMAC_ENCODING = "encoding";

    /**
     * HMAC的header
     */
    String TASK_AUTH_HMAC_KEY_HEADER = "header";

    /**
     * HMAC的secret
     */
    String TASK_AUTH_HMAC_SECRET = "secret";

    /**
     * 字段映射的根目录
     */
    String FIELD_MAPPING_ROOT = "/";


    /**
     * 任务过滤条件值类型 - 值类型
     */
    Integer TASK_FILTER_TYPE_VALUE = 1;

    /**
     * 任务过滤条件值类型 - 字段名
     */
    Integer TASK_FILTER_TYPE_FIELD = 2;

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
     * Job上下文共享数据的标识：webhook请求body
     */
    String JOB_DATA_KEY_WEBHOOK_REQUEST_BODY = "WEBHOOK_REQUEST_BODY";

    /**
     * Job上下文共享数据的标识：业务数据
     */
    String JOB_DATA_KEY_BIZ_DATA = "BIZ_DATA";

    /**
     * Job上下文共享数据的标识：参数数据
     */
    String JOB_DATA_KEY_PARAM_DATA = "PARAM_DATA";

    /**
     * Job上下文共享数据的标识：过滤拦截的数据
     */
    String JOB_DATA_KEY_FILTER_BLOCKED_DATA = "FILTER_BLOCKED_DATA";

    /**
     * Job上下文共享数据的标识：保存的业务数据
     */
    String JOB_DATA_KEY_SAVED_DATA = "SAVED_DATA";

    /**
     * 调度任务的数据标识：触发类型
     */
    String JOB_DATA_KEY_TRIGGER_TYPE = "TRIGGER_TYPE";

    /**
     * 任务输出项：流水线json
     */
    String JOB_DATA_KEY_PIPELINE_JSON = "PIPELINE_JSON";
    /**
     * 任务输出项：原始json
     */
//    String JOB_DATA_KEY_ORIGIN_JSON = "ORIGIN_JSON";

    /**
     * 任务输出项：数据json
     */
    String JOB_DATA_KEY_DATA_JSON = "DATA_JSON";

    /**
     * Job上下文共享数据的标识：Excel文件
     */
    String JOB_DATA_KEY_EXCEL_FILE = "EXCEL_FILE";

    /**
     * Job触发类型：1-手动
     */
    Integer JOB_TRIGGER_TYPE_MANUAL = 1;
    /**
     * Job触发类型：2-定时
     */
    Integer JOB_TRIGGER_TYPE_SCHEDULE = 2;
    /**
     * Job触发类型：3-webhook
     */
    Integer JOB_TRIGGER_TYPE_WEBHOOK = 3;

    /**
     * 手动执行流水线的分组标识
     */
    String JOB_GROUP_MANUAL = "manual";

    /**
     * webhook触发执行流水线的分组标识
     */
    String JOB_GROUP_WEBHOOK = "webhook";

    // ------------------------------ 流水线历史记录常量 ------------------------------

    /**
     * 流水线历史记录的执行状态：0-未开始
     */
    int PIPELINE_HISTORY_STATUS_READY = 0;
    /**
     * 流水线历史记录的执行状态：1-运行中
     */
    int PIPELINE_HISTORY_STATUS_RUNNING = 1;
    /**
     * 流水线历史记录的执行状态：2-中止
     */
    int PIPELINE_HISTORY_STATUS_STOPPED = 2;
    /**
     * 流水线历史记录的执行状态：3-成功
     */
    int PIPELINE_HISTORY_STATUS_SUCCESS = 3;
    /**
     * 流水线历史记录的执行状态：4-失败
     */
    int PIPELINE_HISTORY_STATUS_FAILED = 4;

    // ------------------------------ 业务数据相关常量 ------------------------------

    /**
     * 唯一标识名
     */
    String MONGODB_OBJECT_ID = "_id";

    /**
     * 业务数据字段名称：唯一标识
     */
    String DATA_COLUMN_DATA_ID = "_MD_DATA_ID_";

    /**
     * 业务数据字段名称：最后更新时间
     */
    String DATA_COLUMN_UPDATE_TIME = "_MD_UPDATE_TIME_";

    /**
     * 业务数据历史记录集合后缀
     */
    String DATA_COLUMN_HISTORY = "_MD_HISTORY";

    /**
     * 数据过滤操作：等于
     */
    String DATA_OP_EQ = "=";

    /**
     * 数据过滤操作：等于
     */
    String DATA_OP_NE = "!=";

    /**
     * 数据过滤操作：大于
     */
    String DATA_OP_GT = ">";

    /**
     * 数据过滤操作：大于或等于
     */
    String DATA_OP_GTE = ">=";

    /**
     * 数据过滤操作：小于
     */
    String DATA_OP_LT = "<";

    /**
     * 数据过滤操作：小于或等于
     */
    String DATA_OP_LTE = "<=";

    /**
     * 数据过滤操作：not null
     */
    String DATA_NOT_NULL = "nn";

    /**
     * 数据过滤操作：not empty
     */
    String DATA_NOT_EMPTY = "ne";

    /**
     * 数据过滤操作：模糊匹配
     */
    String DATA_OP_LIKE = "like";

    /**
     * 数据类型：默认，随提供放返回而定
     */
    String DATA_TYPE_DEFAULT = "default";
    /**
     * 数据类型：整数
     */
    String DATA_TYPE_INT = "int";
    /**
     * 数据类型：字符串
     */
    String DATA_TYPE_STRING = "string";
    /**
     * 数据类型：日期
     */
    String DATA_TYPE_DATE = "date";
    /**
     * 数据类型：数字，包括小数
     */
    String DATA_TYPE_NUMBER = "number";

    // ---------- 网络常量 ----------

    /**
     * Http方法枚举
     */
    enum HttpMethod {
        /**
         * GET
         */
        GET,
        /**
         * POST
         */
        POST,
        /**
         * PUT
         */
        PUT,
        /**
         * DELETE
         */
        DELETE
    }

    /**
     * 接口数据类型枚举
     */
    enum ApiDataType {

        /**
         * 接口类型：json
         */
        JSON;
    }

    /**
     * http协议
     */
    String HTTP = "http://";

    /**
     * https协议
     */
    String HTTPS = "https://";
}
