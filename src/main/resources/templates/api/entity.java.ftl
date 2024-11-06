package ${packageName}.entity;

<#list properties as p>
  <#if p.typeClassName == "String" && p.isArray>
import com.baomidou.mybatisplus.annotation.TableField;
import tech.zhiwei.frostmetal.core.jackson.StringArrayTypeHandler;
   <#break>
  </#if>
</#list>
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.${parentEntityPackage}.entity.${parentEntityClass};

<#list classes as c>
 <#if !c?starts_with("java.lang")>
import ${c};
 </#if>
</#list>

/**
 * ${entityName} entity
 *
 * @author ${auth}
 * @since ${date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "${tableName}"<#list properties as p><#if p.typeClassName == "String" && p.isArray>, autoResultMap = true<#break></#if></#list>)
public class ${entityClassName} extends ${parentEntityClass} {
<#list properties as p>
 <#if p.propertyCode != "id">
    /**
     * ${p.propertyName}
     */
  <#if p.typeClassName == "String" && p.isArray>
    @TableField(typeHandler = StringArrayTypeHandler.class)
  </#if>
    private ${p.typeClassName}<#if p.isArray>[]</#if> ${p.propertyCode};

 </#if>
</#list>
}