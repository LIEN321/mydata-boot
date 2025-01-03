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
import tech.zhiwei.frostmetal.system.dto.ParameterDTO;
import tech.zhiwei.frostmetal.system.entity.SysParameter;
import tech.zhiwei.frostmetal.system.service.IParameterService;
import tech.zhiwei.frostmetal.system.vo.ParameterVO;
import tech.zhiwei.frostmetal.system.wrapper.ParameterWrapper;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Collection;
import java.util.List;

/**
 * 系统参数管理 Controller
 *
 * @author LIEN
 * @since 2024/9/1
 */
@RestController
@RequestMapping("/parameter")
@AllArgsConstructor
@Tag(name = "parameter", description = "系统参数管理API")
public class ParameterController {
    private IParameterService parameterService;

    @PostMapping
    @Operation(summary = "新增或更新系统参数", operationId = "saveParameter")
    public R<Long> save(@RequestBody ParameterDTO parameterDTO) {
        return R.data(parameterService.saveParameter(parameterDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询系统参数", operationId = "parameterPage")
    public P<List<ParameterVO>> page(@ParameterObject PageParam pageParam, @RequestParam(required = false) String code, @RequestParam(required = false) String name) {
        LambdaQueryWrapper<SysParameter> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtil.isNotEmpty(code), SysParameter::getCode, code).like(StringUtil.isNotEmpty(name), SysParameter::getName, name);
        return P.page(ParameterWrapper.getInstance().pageVO(parameterService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有系统参数", operationId = "parameterList")
    public R<List<ParameterVO>> list() {
        return R.data(ParameterWrapper.getInstance().listVO(parameterService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "系统参数详情", operationId = "parameterDetail")
    public R<ParameterVO> detail(@PathVariable Long id) {
        return R.data(ParameterWrapper.getInstance().entityVO(parameterService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除系统参数", operationId = "deleteParameter")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(parameterService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除系统参数", operationId = "deleteParameters")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(parameterService.remove(ids));
    }

}
