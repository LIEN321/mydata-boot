package tech.zhiwei.frostmetal.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.core.base.common.ResponseCode;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.core.web.WebUtil;
import tech.zhiwei.tool.json.JsonUtil;

import java.io.IOException;
import java.util.Objects;

/**
 * 签名认证拦截器
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Slf4j
@AllArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (null == AuthUtil.getUser()) {
            log.warn("签名认证失败，请求接口：{}，请求IP：{}，请求参数：{}", request.getRequestURI(), WebUtil.getIP(request), JsonUtil.toJsonString(request.getParameterMap()));

            R<Object> result = R.fail(ResponseCode.UN_AUTHORIZED);
            response.setCharacterEncoding(SysConstant.UTF_8);
            response.addHeader(SysConstant.CONTENT_TYPE_NAME, MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try {
                response.getWriter().write(Objects.requireNonNull(JsonUtil.toJsonString(result)));
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }

            return false;
        }
        return true;
    }

}
