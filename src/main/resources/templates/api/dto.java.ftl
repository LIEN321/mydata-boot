package ${packageName}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.${parentDTOClass};
<#list classes as c>
 <#if !c?starts_with("java.lang")>
import ${c};
 </#if>
</#list>

/**
 * ${entityName} DTO
 *
 * @author ${auth}
 * @since ${date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "${entityName}")
public class ${entityClassName}DTO extends ${parentDTOClass} {
<#list properties as p>
 <#if p.propertyCode != "id">
    @Schema(description = "${p.propertyName}")
    private ${p.typeClassName}<#if p.isArray>[]</#if> ${p.propertyCode};

 </#if>
</#list>
}
