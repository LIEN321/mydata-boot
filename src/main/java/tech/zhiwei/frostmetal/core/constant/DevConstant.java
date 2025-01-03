package tech.zhiwei.frostmetal.core.constant;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * 开发工具相关常量
 *
 * @author LIEN
 * @since 2024/10/5
 */
public interface DevConstant {
    /**
     * 属性类型映射
     */
    Map<String, String> CLASS_TYPE_MAP = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = 4571257865856780075L;

        {
            put("String", "java.lang.String");
            put("Long", "java.lang.String");
            put("Integer", "java.lang.String");
            put("Date", "java.util.Date");
            put("Double", "java.lang.Double");
            put("Boolean", "java.lang.Boolean");
            put("BigDecimal", "java.math.BigDecimal");
        }
    };

    /**
     * 属性类型与mysql数据库字段类型的映射
     */
    Map<String, String> CLASS_MYSQL_TYPE_MAP = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = 4571257865856780075L;

        {
            put("String", "varchar(255)");
            put("Long", "bigint");
            put("Integer", "int");
            put("Date", "datetime");
            put("Double", "double");
            put("Boolean", "tinyint(1)");
            put("BigDecimal", "decimal");
        }
    };

    /**
     * 组件名映射
     */
    Map<String, String> COMPONENT_MAP = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -2641406140513364199L;

        {
            put("input", "ProFormText");
            put("textArea", "ProFormTextArea");
            put("date", "ProFormDatePicker");
            put("time", "ProFormDateTimePicker");
            put("select", "ProFormSelect");
            put("treeSelect", "ProFormTreeSelect");
            put("radio", "ProFormRadio");
            put("checkbox", "ProFormCheckbox");
            put("switch", "ProFormSwitch");
        }
    };

    // -------------------- 继承父类的类型 --------------------
    /**
     * 继承父类的模式：id
     * Entity只有默认的主键id
     */
    String EXTEND_MODE_ID = "id";

    /**
     * 继承父类的模式：base
     * 在id基础上增加：create_user、create_time、update_user、update_time、status、is_deleted、create_department、update_department
     */
    String EXTEND_MODE_BASE = "base";

    /**
     * 继承父类的模式：tree
     * 在base基础上增加：parent_id、id_tree_path、is_leaf
     */
    String EXTEND_MODE_TREE = "tree";

    /**
     * 继承父类的模式：tenant
     * 在base基础上增加：tenant_id
     */
    String EXTEND_MODE_TENANT = "tenant";

    /**
     * 继承父类的模式：tree_tenant
     * 在tree基础上增加：tenant_id
     */
    String EXTEND_MODE_TREE_TENANT = "tree_tenant";

    // -------------------- 查询条件 --------------------
    /**
     * 查询条件
     */
    Map<String, String> SEARCH_TYPE = new HashMap<>() {
        @Serial
        private static final long serialVersionUID = -1606393762279363869L;

        {
            put("eq", "eq");
            put("like", "like");
//            put("between", "between");
            put("ne", "ne");
            put("gt", "gt");
            put("ge", "ge");
            put("lt", "lt");
            put("le", "le");
            put("likeLeft", "likeLeft");
            put("likeRight", "likeRight");
        }
    };
}
