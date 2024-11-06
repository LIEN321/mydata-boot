package tech.zhiwei.frostmetal.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import tech.zhiwei.frostmetal.system.dto.TenantDTO;
import tech.zhiwei.frostmetal.system.entity.Tenant;
import tech.zhiwei.frostmetal.system.service.ITenantService;
import tech.zhiwei.frostmetal.system.vo.TenantVO;
import tech.zhiwei.frostmetal.system.wrapper.TenantWrapper;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * 系统租户 Controller
 *
 * @author LIEN
 * @since 2024/11/02
 */
@RestController
@RequestMapping("/tenant")
@AllArgsConstructor
@Tag(name = "tenant", description = "系统租户API")
public class TenantController {
    private ITenantService tenantService;

    @PostMapping
    @Operation(summary = "新增或更新系统租户", operationId = "saveTenant")
    public R<Long> save(@RequestBody TenantDTO TenantDTO) {
        return R.data(tenantService.saveTenant(TenantDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询系统租户", operationId = "tenantPage")
    public P<List<TenantVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) String tenantCode
            , @RequestParam(required = false) String tenantName
    ) {
        LambdaQueryWrapper<Tenant> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(ObjectUtil.isNotNull(tenantCode), Tenant::getTenantCode, tenantCode);
        queryWrapper.like(ObjectUtil.isNotNull(tenantName), Tenant::getTenantName, tenantName);

        return P.page(TenantWrapper.getInstance().pageVO(tenantService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有系统租户", operationId = "tenantList")
    public R<List<TenantVO>> list() {
        return R.data(TenantWrapper.getInstance().listVO(tenantService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "系统租户详情", operationId = "tenantDetail")
    @Parameter(name = "id", description = "记录id")
    public R<TenantVO> detail(@PathVariable Long id) {
        return R.data(TenantWrapper.getInstance().entityVO(tenantService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除系统租户", operationId = "deleteTenant")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(tenantService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除系统租户", operationId = "deleteTenants")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(tenantService.remove(ids));
    }
}
