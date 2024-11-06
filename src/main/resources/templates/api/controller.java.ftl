package ${packageName}.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
<#if extendMode == "id">
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
</#if>
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.core.base.common.P;
import tech.zhiwei.frostmetal.core.base.common.PageParam;
import tech.zhiwei.frostmetal.core.base.common.R;
import ${packageName}.dto.${entityClassName}DTO;
import ${packageName}.entity.${entityClassName};
import ${packageName}.service.I${entityClassName}Service;
import ${packageName}.vo.${entityClassName}VO;
import ${packageName}.wrapper.${entityClassName}Wrapper;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * ${entityName} Controller
 *
 * @author ${auth}
 * @since ${date}
 */
@RestController
@RequestMapping("/${entityCode}")
@AllArgsConstructor
@Tag(name = "${entityCode}", description = "${entityName}API")
public class ${entityClassName}Controller {
    private I${entityClassName}Service ${entityCode}Service;

    @PostMapping
    @Operation(summary = "新增或更新${entityName}", operationId = "save${entityClassName}")
    public R<Long> save(@RequestBody ${entityClassName}DTO ${entityClassName}DTO) {
        return R.data(${entityCode}Service.save${entityClassName}(${entityClassName}DTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询${entityName}", operationId = "${entityCode}Page")
    public P<List<${entityClassName}VO>> page(@ParameterObject PageParam pageParam
    <#list properties as p>
        <#if p.isSearch>
            , @RequestParam(required = false) String ${p.propertyCode}
        </#if>
    </#list>
    ) {
        LambdaQueryWrapper<${entityClassName}> queryWrapper = Wrappers.lambdaQuery();
<#list properties as p>
    <#if p.isSearch>
        <#if p.searchType != "" && SEARCH_TYPE[p.searchType]??>
        queryWrapper.${p.searchType}(ObjectUtil.isNotNull(${p.propertyCode}), ${entityClassName}::get${p.propertyCode?cap_first}, ${p.propertyCode});
        <#else>
        // TODO 此处未生成代码，原因：不支持的搜索条件 "${p.searchType}"，请手动处理
        </#if>
    </#if>
</#list>

    <#if extendMode == "id">
        IPage<Demo> page = new Page<>(pageParam.getCurrent(), pageParam.getPageSize());
        return P.page(DemoWrapper.getInstance().pageVO(demoService.page(page, queryWrapper)));
    <#else>
        return P.page(${entityClassName}Wrapper.getInstance().pageVO(${entityCode}Service.page(queryWrapper, pageParam)));
    </#if>
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有${entityName}", operationId = "${entityCode}List")
    public R<List<${entityClassName}VO>> list() {
        return R.data(${entityClassName}Wrapper.getInstance().listVO(${entityCode}Service.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "${entityName}详情", operationId = "${entityCode}Detail")
    @Parameter(name = "id", description = "记录id")
    public R<${entityClassName}VO> detail(@PathVariable Long id) {
        return R.data(${entityClassName}Wrapper.getInstance().entityVO(${entityCode}Service.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除${entityName}", operationId = "delete${entityClassName}")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
    <#if extendMode == "id">
        return R.status(${entityCode}Service.removeById(id));
    <#else>
        return R.status(${entityCode}Service.remove(id));
    </#if>
    }

    @DeleteMapping
    @Operation(summary = "批量删除${entityName}", operationId = "delete${entityClassName}s")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
    <#if extendMode == "id">
        return R.status(${entityCode}Service.removeByIds(ids));
    <#else>
        return R.status(${entityCode}Service.remove(ids));
    </#if>
    }
}
