package tech.zhiwei.tool.http;

import cn.hutool.http.HttpConfig;
import cn.hutool.http.HttpConnection;

import java.nio.charset.Charset;

/**
 * Http响应类
 *
 * @author LIEN
 * @since 2025/6/13
 */
public class HttpResponse extends cn.hutool.http.HttpResponse {
    /**
     * 构造
     *
     * @param httpConnection {@link HttpConnection}
     * @param config         Http配置
     * @param charset        编码，从请求编码中获取默认编码
     * @param isAsync        是否异步
     * @param isIgnoreBody   是否忽略读取响应体
     * @since 3.1.2
     */
    protected HttpResponse(HttpConnection httpConnection, HttpConfig config, Charset charset, boolean isAsync, boolean isIgnoreBody) {
        super(httpConnection, config, charset, isAsync, isIgnoreBody);
    }
}
