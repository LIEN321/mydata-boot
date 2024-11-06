package tech.zhiwei.frostmetal.core.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.zhiwei.tool.lang.StringPool;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.function.Predicate;

/**
 * Web工具类
 *
 * @author LIEN
 * @since 2024/8/27
 */
public class WebUtil {
    private static final String[] IP_HEADER_NAMES = new String[]{
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };
    private static final Predicate<String> IP_PREDICATE = (ip) -> StringUtil.isBlank(ip) || StringPool.UNKNOWN.equalsIgnoreCase(ip);

    /**
     * 获取当前请求对象
     *
     * @return 请求对象
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 获取ip
     *
     * @return ip
     */
    public static String getIP() {
        return getIP(WebUtil.getRequest());
    }

    /**
     * 获取ip
     *
     * @param request HttpServletRequest
     * @return {String}
     */
    @Nullable
    public static String getIP(@Nullable HttpServletRequest request) {
        if (request == null) {
            return StringPool.EMPTY;
        }
        String ip = null;
        for (String ipHeader : IP_HEADER_NAMES) {
            ip = request.getHeader(ipHeader);
            if (!IP_PREDICATE.test(ip)) {
                break;
            }
        }
        if (IP_PREDICATE.test(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取服务端的完整路径
     *
     * @param suffix 自定义后缀
     * @return 完整路径
     */
    public static String getRealPath(String suffix) {
        HttpServletRequest request = getRequest();
        return request.getServletContext().getRealPath("/") + suffix;
    }
}
