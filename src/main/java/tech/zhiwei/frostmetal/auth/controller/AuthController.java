package tech.zhiwei.frostmetal.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.auth.bean.AuthUser;
import tech.zhiwei.frostmetal.auth.dto.LoginDTO;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.system.cache.SysCache;
import tech.zhiwei.frostmetal.system.entity.Tenant;
import tech.zhiwei.frostmetal.system.entity.User;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.lang.AssertUtil;

/**
 * 用户认证 Controller
 *
 * @author LIEN
 * @since 2024/8/26
 */
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Tag(name = "auth", description = "auth api")
public class AuthController {
    private final IUserService userService;

    @PostMapping("/login")
    @Operation(summary = "账号登录", operationId = "login")
    public R<Object> login(@RequestBody LoginDTO loginDTO) {
        Tenant tenant = SysCache.getTenant(loginDTO.getCode());
        AssertUtil.notNull(tenant, "登录失败，请检查登录账号！");
        User user = userService.verify(tenant.getTenantId(), loginDTO.getUsername(), loginDTO.getPassword());
        if (user != null) {
            AuthUser authUser = BeanUtil.copyProperties(user, AuthUser.class);
            AuthUtil.login(authUser, loginDTO.getAutoLogin());
            return R.success();
        }
        return R.fail("登录失败，请检查登录账号！");
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", operationId = "logout")
    public R<Object> logout() {
        AuthUtil.logout();
        return R.data(true);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", operationId = "currentUser")
    public R<AuthUser> currentUser() {
        return R.data(AuthUtil.getUser());
    }
}
