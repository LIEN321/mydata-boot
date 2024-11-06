package tech.zhiwei.frostmetal.core.constant;

/**
 * 系统常量
 *
 * @author LIEN
 * @since 2024/8/26
 */
public interface SysConstant {
    /**
     * 编码
     */
    String UTF_8 = "UTF-8";

    /**
     * contentType
     */
    String CONTENT_TYPE_NAME = "Content-type";

    /**
     * 逻辑删除状态，0-未删除
     */
    int NOT_DELETED = 0;

    /**
     * 逻辑删除状态，1-已删除
     */
    int IS_DELETED = 1;

    /**
     * 业务状态，1-启用
     */
    int STATUS_ENABLED = 1;

    /**
     * 业务状态，0-禁用
     */
    int STATUS_DISABLED = 0;

    // -------------------- 数据库相关 --------------------

    /**
     * 用户表
     */
    String TABLE_SYS_USER = "sys_user";

    /**
     * 部门表
     */
    String TABLE_SYS_DEPARTMENT = "sys_department";

    /**
     * 用户角色关联表
     */
    String TABLE_SYS_USER_ROLE = "sys_user_role";

    /**
     * 菜单表
     */
    String TABLE_SYS_MENU = "sys_menu";

    /**
     * 角色表
     */
    String TABLE_SYS_ROLE = "sys_role";

    /**
     * 角色菜单关联表
     */
    String TABLE_SYS_ROLE_MENU = "sys_role_menu";

    /**
     * 系统参数表
     */
    String TABLE_SYS_PARAMETER = "sys_parameter";

    /**
     * 系统接口表
     */
    String TABLE_SYS_API = "sys_api";

    /**
     * 开发工具：业务实体表
     */
    String TABLE_DEV_ENTITY = "dev_entity";

    /**
     * 开发工具：业务实体属性表
     */
    String TABLE_DEV_ENTITY_PROPERTY = "dev_entity_property";

    /**
     * 加盐字符串长度
     */
    int SALT_LENGTH = 32;

    /**
     * 登录密码的字符串长度
     */
    int PASSWORD_LENGTH = 16;

    /**
     * parent_id字段名
     */
    String COLUMN_PARENT_ID = "parent_id";

    /**
     * id_tree_path字段名
     */
    String COLUMN_ID_TREE_PATH = "id_tree_path";

    /**
     * tree结构默认parent_id值
     */
    Long DEFAULT_PARENT_ID = null;

    // -------------------- 缓存相关 --------------------

    /**
     * 缓存统一前缀
     */
    String CACHE_PREFIX = "zhiwei:frostmetal:";
}
