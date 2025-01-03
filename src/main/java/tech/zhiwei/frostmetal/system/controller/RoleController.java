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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.core.base.common.P;
import tech.zhiwei.frostmetal.core.base.common.PageParam;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.system.dto.RoleDTO;
import tech.zhiwei.frostmetal.system.dto.RoleGrantDTO;
import tech.zhiwei.frostmetal.system.entity.Role;
import tech.zhiwei.frostmetal.system.service.IRoleService;
import tech.zhiwei.frostmetal.system.vo.RoleSelectVO;
import tech.zhiwei.frostmetal.system.vo.RoleVO;
import tech.zhiwei.frostmetal.system.wrapper.RoleWrapper;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Collection;
import java.util.List;

/**
 * 角色管理 Controller
 *
 * @author LIEN
 * @since 2024/8/27
 */
@RestController
@RequestMapping("/role")
@AllArgsConstructor
@Tag(name = "role", description = "角色管理API")
public class RoleController {
    private IRoleService roleService;

    @PostMapping
    @Operation(summary = "新增或更新角色", operationId = "saveRole")
    public R<Long> save(@RequestBody RoleDTO roleDTO) {
        return R.data(roleService.saveRole(roleDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询角色", operationId = "rolePage")
    public P<List<RoleVO>> page(@ParameterObject PageParam pageParam, @RequestParam(required = false) String code,
        @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Role> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtil.isNotEmpty(code), Role::getCode, code)
            .like(StringUtil.isNotEmpty(name), Role::getName, name);
        return P.page(RoleWrapper.getInstance().pageVO(roleService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有角色", operationId = "roleList")
    public R<List<RoleVO>> list() {
        return R.data(RoleWrapper.getInstance().listVO(roleService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "角色详情", operationId = "roleDetail")
    @Parameter(name = "id", description = "记录id")
    public R<RoleVO> detail(@PathVariable Long id) {
        return R.data(RoleWrapper.getInstance().entityVO(roleService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除角色", operationId = "deleteRole")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(roleService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除角色", operationId = "deleteRoles")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(roleService.remove(ids));
    }

    @GetMapping("/select")
    @Operation(summary = "查询所有角色", operationId = "roleSelect")
    public List<RoleSelectVO> select() {
        return RoleWrapper.getInstance().selectVOList(roleService.list());
    }

    @PutMapping("/grant")
    @Operation(summary = "授权角色权限", operationId = "grantRole")
    public R<Boolean> grant(@RequestBody RoleGrantDTO roleGrantDTO) {
        return R.status(roleService.grant(roleGrantDTO.getRoleIds(), roleGrantDTO.getMenuIds()));
    }
}
