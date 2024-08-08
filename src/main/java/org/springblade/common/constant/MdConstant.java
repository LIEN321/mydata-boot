package org.springblade.common.constant;

/**
 * mydata 业务常量
 *
 * @author LIEN
 * @since 2022/7/9
 */
public interface MdConstant {
    // 接口前缀名称
    String API_PREFIX_MANAGE = "/mydata-manage";

    // 默认分页尺寸
    int PAGE_SIZE = 10;

    // 默认有效
    int ENABLED = 1;

    // 默认无效
    int DISABLED = 0;

    // 默认密文
    String SECURE_STRING = "****************";

    String TEMP_DIR = "mydata";

    // ---------- 字段常量 ----------
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

    // ---------- 数据常量 ----------
    /**
     * 数据操作类型：数据提供者
     */
    int DATA_PRODUCER = 1;

    /**
     * 数据操作类型：数据消费者
     */
    int DATA_CONSUMER = 2;

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
     * 参数名：条件名
     */
    String PARAM_KEY = "k";

    /**
     * 参数名：条件操作
     */
    String PARAM_OP = "op";

    /**
     * 参数名：条件值
     */
    String PARAM_VALUE = "v";

    /**
     * 参数名：条件类型
     */
    String PARAM_TYPE = "t";

    /**
     * 分批参数名，递增区间
     */
    String BATCH_INC_STEP = "step";

    /**
     * 业务数据字段名称：最后更新时间
     */
    String DATA_COLUMN_UPDATE_TIME = "_MD_UPDATE_TIME_";

    /**
     * 业务数据字段名称：最后更新时间
     */
    String DATA_COLUMN_BATCH_ID = "_MD_BATCH_ID_";

    /**
     * 业务数据字段名称：唯一标识
     */
    String DATA_COLUMN_DATA_ID = "_MD_DATA_ID_";

    /**
     * 业务数据字段名称：唯一标识
     */
    String DATA_COLUMN_HISTORY = "_MD_HISTORY";

    /**
     * 业务数据常量值：任务最后成功时间
     */
    String DATA_VALUE_TASK_LAST_SUCCESS_TIME = "_MD_TASK_LAST_SUCCESS_";

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

    // ---------- 任务常量 ----------

    /**
     * 停止状态
     */
    int TASK_STATUS_STOPPED = 3;

    /**
     * 已启动状态
     */
    int TASK_STATUS_STARTED = 1;

    /**
     * 失败状态
     */
    int TASK_STATUS_FAILED = 2;

    /**
     * 任务结果，0-失败
     */
    int TASK_RESULT_FAILED = 0;

    /**
     * 任务结果，1-成功
     */
    int TASK_RESULT_SUCCESS = 1;

    /**
     * 是否唯一标识
     */
    Integer IS_ID_FIELD = 1;

    /**
     * API 每一轮发送的数据量
     */
    int ROUND_DATA_COUNT = 1000;

    /**
     * 每个任务最多失败次数
     */
    int TASK_MAX_FAIL_COUNT = 5;

    /**
     * 不是订阅任务
     */
    Integer TASK_IS_NOT_SUBSCRIBED = 0;

    /**
     * 是订阅任务
     */
    Integer TASK_IS_SUBSCRIBED = 1;

    /**
     * 定时任务 默认执行次数
     */
    int TASK_JOB_DEFAULT_TIMES = Integer.MAX_VALUE;

    /**
     * 提供数据模式，调用API
     */
    Integer TASK_PRODUCE_MODE_API = 1;

    /**
     * 提供数据模式，接收推送
     */
    Integer TASK_PRODUCE_MODE_PUSH = 2;

    /**
     * 消费模式，调用API
     */
    Integer TASK_CONSUME_MODE_API = 1;

    /**
     * 消费模式，发送邮件
     */
    Integer TASK_CONSUME_MODE_EMAIL = 2;

    /**
     * 跳过接口返回两次相同数据的错误
     */
    Integer TASK_SKIP_SAME_DATA_ERROR = 1;

    /**
     * 任务过滤条件值类型 - 值类型
     */
    Integer TASK_FILTER_TYPE_VALUE = 1;

    /**
     * 任务过滤条件值类型 - 字段名
     */
    Integer TASK_FILTER_TYPE_FIELD = 2;

    /**
     * 任务失败重试间隔
     */
    String[] TASK_FAILED_PERIOD = {"0 0/1 * * * ?", "0 0/15 * * * ?", "0 0 0/1 * * ?", "0 0 0/3 * * ?"};

    /**
     * 单条记录消费模式 - 单个对象对象
     */
    Integer TASK_SINGLE_MODE_OBJECT = 1;
    /**
     * 单条记录消费模式 - 集合模式
     */
    Integer TASK_SINGLE_MODE_COLLECTION = 2;

    /**
     * 认证类型，无需认证
     */
    Integer TASK_AUTH_TYPE_NONE = 0;
    /**
     * 认证类型，api key
     */
    Integer TASK_AUTH_TYPE_API_KEY = 1;

    /**
     * api key 的名称
     */
    String TASK_AUTH_API_KEY_HEADER = "keyHeader";
    /**
     * api key value 的名称
     */
    String TASK_AUTH_API_KEY_VALUE = "keyValue";

    /**
     * 认证类型，basic auth
     */
    Integer TASK_AUTH_TYPE_BASIC = 2;

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
    String TASK_AUTH_HMAC_KEY_HEADER = TASK_AUTH_API_KEY_HEADER;

    /**
     * HMAC的secret
     */
    String TASK_AUTH_HMAC_SECRET = "secret";

    /**
     * 字段映射的根目录
     */
    String FIELD_MAPPING_ROOT = "/";

    /**
     *
     */
    String MONGODB_OBJECT_ID = "_id";
}
