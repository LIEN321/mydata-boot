package tech.zhiwei.frostmetal.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.core.base.common.ResponseCode;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.core.web.WebUtil;
import tech.zhiwei.frostmetal.system.cache.SysCache;
import tech.zhiwei.frostmetal.system.entity.RoleMenu;
import tech.zhiwei.frostmetal.system.entity.SysApi;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 接口权限校验的拦截器
 *
 * @author LIEN
 * @since 2024/9/16
 */
@Slf4j
public class ApiAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取当前的访问地址
        String method = request.getMethod().toUpperCase();
        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();
        if (StringUtil.isNotEmpty(pathInfo)) {
            path = path + pathInfo;
        }

        // 根据访问地址 查询接口
        SysApi sysApi = SysCache.getSysApi(method, path);
        if (sysApi == null) {
            // 接口未登记，可直接放行
            return true;
        }

        // 是否放行的标记
        boolean isPassed = false;

        // 查询接口所属菜单关联的角色
        Long menuId = sysApi.getMenuId();
        List<RoleMenu> roleMenus = SysCache.getRoleMenuByMenuId(menuId);

        // 若没有关联的角色，则不能放行
        // 若有关联的角色，则校验当前用户的角色 是否属于其中
        if (CollectionUtil.isNotEmpty(roleMenus)) {
            List<Long> roleIds = roleMenus.stream().map(RoleMenu::getRoleId).toList();
            Long userRoleId = AuthUtil.getRoleId();
            if (roleIds.contains(userRoleId)) {
                isPassed = true;
            }
        }

        // 若未通过，则返回403
        if (!isPassed) {
            log.warn("接口权限认证失败，请求接口：{}，请求IP：{}，请求参数：{}", request.getRequestURI(), WebUtil.getIP(request), JsonUtil.toJsonString(request.getParameterMap()));

            R<Object> result = R.fail(ResponseCode.REQ_REJECT);
            response.setCharacterEncoding(SysConstant.UTF_8);
            response.addHeader(SysConstant.CONTENT_TYPE_NAME, MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            try {
                response.getWriter().write(Objects.requireNonNull(JsonUtil.toJsonString(result)));
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }

        return isPassed;
    }
}
