package tech.zhiwei.frostmetal.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
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
import tech.zhiwei.frostmetal.system.dto.SysApiDTO;
import tech.zhiwei.frostmetal.system.entity.SysApi;
import tech.zhiwei.frostmetal.system.service.ISysApiService;
import tech.zhiwei.frostmetal.system.vo.SysApiVO;
import tech.zhiwei.frostmetal.system.wrapper.SysApiWrapper;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Collection;
import java.util.List;

/**
 * 系统接口管理 Controller
 *
 * @author LIEN
 * @since 2024/9/1
 */
@RestController
@RequestMapping("/sys_api")
@AllArgsConstructor
@Tag(name = "api", description = "系统接口管理API")
public class SysApiController {
    private ISysApiService sysApiService;

    @PostMapping
    @Operation(summary = "新增或更新系统接口", operationId = "saveSysApi")
    public R<Long> save(@RequestBody SysApiDTO sysApiDTO) {
        return R.data(sysApiService.saveSysApi(sysApiDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询系统接口", operationId = "sysApiPage")
    public P<List<SysApiVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) Long menuId
            , @RequestParam(required = false) String code
            , @RequestParam(required = false) String name) {
        LambdaQueryWrapper<SysApi> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ObjectUtil.isNotNull(menuId), SysApi::getMenuId, menuId)
                .like(StringUtil.isNotEmpty(code), SysApi::getCode, code)
                .like(StringUtil.isNotEmpty(name), SysApi::getName, name);
        return P.page(SysApiWrapper.getInstance().pageVO(sysApiService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有系统接口", operationId = "sysApiList")
    public R<List<SysApiVO>> list() {
        return R.data(SysApiWrapper.getInstance().listVO(sysApiService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "系统接口详情", operationId = "sysApiDetail")
    public R<SysApiVO> detail(@PathVariable Long id) {
        return R.data(SysApiWrapper.getInstance().entityVO(sysApiService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除系统接口", operationId = "deleteSysApi")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(sysApiService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除系统接口", operationId = "deleteSysApis")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(sysApiService.remove(ids));
    }

}
