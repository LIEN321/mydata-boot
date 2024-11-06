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
import tech.zhiwei.frostmetal.system.dto.UserDTO;
import tech.zhiwei.frostmetal.system.entity.User;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.frostmetal.system.vo.UserVO;
import tech.zhiwei.frostmetal.system.wrapper.UserWrapper;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Collection;
import java.util.List;

/**
 * 用户管理 Controller
 *
 * @author LIEN
 * @since 2024/8/26
 */
@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Tag(name = "user", description = "用户管理API")
public class UserController {
    private IUserService userService;

    @PostMapping
    @Operation(summary = "新增或更新用户", operationId = "saveUser")
    public R<Long> save(@RequestBody UserDTO userDTO) {
        return R.data(userService.saveUser(userDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询用户", operationId = "userPage")
    public P<List<UserVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) Long departmentId
            , @RequestParam(required = false) String code
            , @RequestParam(required = false) String name) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ObjectUtil.isNotNull(departmentId), User::getDepartmentId, departmentId)
                .like(StringUtil.isNotEmpty(code), User::getCode, code)
                .like(StringUtil.isNotEmpty(name), User::getName, name);
        return P.page(UserWrapper.getInstance().pageVO(userService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情", operationId = "userDetail")
    public R<UserVO> detail(@PathVariable Long id) {
        return R.data(UserWrapper.getInstance().entityVO(userService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除用户", operationId = "deleteUser")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(userService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除用户", operationId = "deleteUsers")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(userService.remove(ids));
    }

    @PostMapping("/resetPassword/{id}")
    public R<String> resetPassword(@PathVariable Long id) {
        return R.data(userService.resetPassword(id));
    }
}
