package tech.zhiwei.tool.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.Map;

/**
 * Http请求工具类
 *
 * @author LIEN
 * @since 2024/11/21
 */
public class HttpUtil extends cn.hutool.http.HttpUtil {

    public static HttpResponse send(String method, String url, Map<String, String> params, Map<String, String> headers, Map<String, String> reqForms, String reqBody) {
        // 拼接url后面的参数
        if (CollectionUtil.isNotEmpty(params)) {
            StringBuffer paramString = new StringBuffer();
            params.forEach((k, v) -> {
                paramString.append(StringUtil.format("&{}={}", k, v));
            });

            if (url.contains("?")) {
                url = url + paramString;
            } else {
                url = url + "?" + paramString.substring(1);
            }
        }

        HttpRequest request = HttpUtil.createRequest(Method.valueOf(method), url);

        // 设置请求header
        request.addHeaders(headers);

        // 若请求form有效，则用reqForm
        if (MapUtil.isNotEmpty(reqForms)) {
            request.formStr(reqForms);
        }

        // 若请求body有效，则用reqBody
        if (StringUtil.isNotEmpty(reqBody)) {
            request.body(reqBody);
        }
        return request.execute();
    }

}
