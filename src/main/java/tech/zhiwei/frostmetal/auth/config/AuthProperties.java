package tech.zhiwei.frostmetal.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 授权配置
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@ConfigurationProperties("frostmetal.secure.auth")
public class AuthProperties {
    /**
     * 默认放行规则
     */
    private final List<String> defaultExcludePatterns = new ArrayList<>();
    /**
     * 默认启动状态
     */
    private Boolean enabled = false;
    /**
     * 用户自定义放行地址
     */
    private List<String> skipUrl = new ArrayList<>();

    public AuthProperties() {
        this.defaultExcludePatterns.add("/auth/**");
        this.defaultExcludePatterns.add("/token/**");
        // -------------------- swagger --------------------
        this.defaultExcludePatterns.add("/doc.html");
        this.defaultExcludePatterns.add("/swagger-ui/**");
        this.defaultExcludePatterns.add("/v3/api-docs/**");
        this.defaultExcludePatterns.add("/swagger-resources");
        this.defaultExcludePatterns.add("/favicon.ico");
        this.defaultExcludePatterns.add("/webjars/**");
        this.defaultExcludePatterns.add("/error/**");
        // --------------------------------------------------
    }
}
