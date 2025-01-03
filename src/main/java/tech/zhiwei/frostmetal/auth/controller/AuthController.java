package tech.zhiwei.frostmetal.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import tech.zhiwei.frostmetal.core.base.common.ResponseCode;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.core.redis.RedisUtil;
import tech.zhiwei.frostmetal.system.entity.Tenant;
import tech.zhiwei.frostmetal.system.entity.User;
import tech.zhiwei.frostmetal.system.service.ITenantService;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.StringUtil;

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
    /**
     * redis记录登录账号的key
     * 第一个{} 是租户id，即tenant_id
     * 第二个{} 是登录账号名，即user_name
     */
    private static final String REDIS_KEY_LOGIN_ATTEMPTS = SysConstant.CACHE_PREFIX + "auth:LOGIN_ATTEMPTS_{}_{}";
    /**
     * 登录失败最大次数
     */
    private static final int MAX_ATTEMPTS = 10;
    /**
     * 登录失败，锁定账号10分钟
     */
    private static final long LOCK_TIME = 10 * 60;

    private final IUserService userService;
    private final ITenantService tenantService;
    private final RedisUtil redisUtil;

    @PostMapping("/login")
    @Operation(summary = "账号登录", operationId = "login")
    public R<Object> login(@Valid @RequestBody LoginDTO loginDTO) {
        Tenant tenant = tenantService.findByCode(loginDTO.getCode());
        AssertUtil.notNull(tenant, "登录失败，无效的租户编号！");

        // 构造 Redis key
        String redisKey = StringUtil.format(REDIS_KEY_LOGIN_ATTEMPTS, tenant.getTenantId(), loginDTO.getUsername());
        // 检查是否已被锁定
        Integer attempts = (Integer) redisUtil.get(redisKey);
        if (attempts != null && attempts > MAX_ATTEMPTS) {
            return R.fail(ResponseCode.REQ_REJECT, "登录失败：账户已被锁定，请稍后再试！");
        }

        User user = userService.verify(tenant.getTenantId(), loginDTO.getUsername(), loginDTO.getPassword());
        if (user != null) {
            // 登录成功
            AuthUser authUser = BeanUtil.copyProperties(user, AuthUser.class);
            AuthUtil.login(authUser, loginDTO.getAutoLogin());

            // 删除失败记录
            redisUtil.del(redisKey);

            return R.success();
        }

        // 登录失败，增加失败次数
        long failedCount = redisUtil.incr(redisKey, 1);
        if (failedCount == 1L) {
            // 新创建，设置过期时间
            redisUtil.expire(redisKey, LOCK_TIME);
        }

        String message;
        if (failedCount < MAX_ATTEMPTS) {
            message = StringUtil.format("登录失败，请检查登录账号，还有{}次机会 否则将会锁定！", (MAX_ATTEMPTS - failedCount), MAX_ATTEMPTS);
        } else {
            message = "登录失败：账户已被锁定，请稍后再试！";
        }

        return R.fail(message);
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
