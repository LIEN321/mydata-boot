package ${packageName}.vo;

<#list properties as p>
    <#if p.typeClassName == "Long" || p.typeClassName == "BigDecimal">
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
        <#break>
    </#if>
</#list>
<#list properties as p>
    <#if p.typeClassName == "Date">
import com.fasterxml.jackson.annotation.JsonFormat;
        <#break>
    </#if>
</#list>
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.${parentVOClass};
<#list classes as c>
    <#if !c?starts_with("java.lang")>
import ${c};
    </#if>
</#list>

/**
 * ${entityName} VO
 *
 * @author ${auth}
 * @since ${date}
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Schema(description = "${entityName}")
public class ${entityClassName}VO extends ${parentVOClass} {
<#list properties as p>
 <#if p.propertyCode != "id">
    @Schema(description = "${p.propertyName}")
  <#if p.typeClassName == "Long">
    @JsonSerialize(using = ToStringSerializer.class)
   <#elseif p.typeClassName == "Date">
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  </#if>
    private ${p.typeClassName}<#if p.isArray>[]</#if> ${p.propertyCode};

 </#if>
</#list>
}
