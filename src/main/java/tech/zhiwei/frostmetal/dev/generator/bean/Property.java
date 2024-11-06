package tech.zhiwei.frostmetal.dev.generator.bean;

import lombok.Data;
import tech.zhiwei.frostmetal.core.constant.DevConstant;
import tech.zhiwei.tool.lang.StringUtil;

/**
 * 生成代码的实体属性
 *
 * @author LIEN
 * @since 2024/9/29
 */
@Data
public class Property {
    public Property(String propertyCode, String propertyName, String typeClassName) {
        this(propertyCode, propertyName, typeClassName, false, false, null, false, null, false);
    }

    public Property(String propertyCode, String propertyName, String typeClassName, Boolean isList, Boolean isSearch,
                    String searchType, Boolean isForm, String componentName, Boolean isRequired) {
        this.propertyCode = StringUtil.toCamelCase(propertyCode);
        this.propertyCodeUnderlineCase = StringUtil.toUnderlineCase(this.propertyCode);
        this.propertyName = propertyName;
        this.typeClassName = typeClassName;
        this.isList = isList;
        this.isSearch = isSearch;
        this.searchType = searchType;
        this.isForm = isForm;
        this.isRequired = isRequired;

        if (StringUtil.isNotEmpty(typeClassName)) {
            this.typeClassFullName = DevConstant.CLASS_TYPE_MAP.get(typeClassName);
            this.tableFieldType = DevConstant.CLASS_MYSQL_TYPE_MAP.get(typeClassName);
        }
        if (StringUtil.isNotEmpty(componentName)) {
            this.componentName = DevConstant.COMPONENT_MAP.get(componentName);
        }

        this.isArray = "checkbox".equals(componentName);
    }

    /**
     * 属性英文名，统一转为 小写字母开头 大写字母开头分隔单词，例如name, hello_world -> helloWorld
     */
    private String propertyCode;

    /**
     * 属性英文名，统一转为下划线方式分隔单词，例如helloWorld -> hello_world
     */
    private String propertyCodeUnderlineCase;

    /**
     * 属性标题名称
     */
    private String propertyName;

    /**
     * 属性类型的类名
     */
    private String typeClassName;

    /**
     * 属性类型的完整类名：package.ClassName
     */
    private String typeClassFullName;

    /**
     * 是否显示在列表：0-不显示，1-显示
     */
    private Boolean isList = false;

    /**
     * 是否作为查询条件：0-不是，1-是
     */
    private Boolean isSearch = false;

    /**
     * 查询类型：equals、like、between…
     */
    private String searchType;

    /**
     * 是否显示在表单：0-不显示，1-显示
     */
    private Boolean isForm = false;

    /**
     * 前端组件名
     */
    private String componentName;

    /**
     * 是否必填
     */
    private Boolean isRequired = false;

    /**
     * 是否为数组
     */
    private Boolean isArray = false;

    /**
     * 数据库字段类型
     */
    private String tableFieldType;
}
