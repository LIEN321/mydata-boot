package tech.zhiwei.tool.http;

import cn.hutool.core.net.url.UrlBuilder;

/**
 * Http请求类
 *
 * @author LIEN
 * @since 2025/6/13
 */
public class HttpRequest extends cn.hutool.http.HttpRequest {
    public HttpRequest(String url) {
        super(url);
    }

    public HttpRequest(UrlBuilder url) {
        super(url);
    }
}
