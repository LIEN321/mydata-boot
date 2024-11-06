package tech.zhiwei.frostmetal.core.tenant.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tech.zhiwei.frostmetal.core.mybatis.MybatisPlusConfiguration;
import tech.zhiwei.frostmetal.core.tenant.handler.TenantHandler;

/**
 * 租户配置类
 *
 * @author LIEN
 * @since 2024/11/4
 */
@AutoConfiguration(before = MybatisPlusConfiguration.class)
@EnableConfigurationProperties(TenantProperties.class)
public class TenantConfiguration {
    /**
     * 多租户的处理类
     *
     * @param tenantProperties 多租户配置
     * @return TenantLineHandler
     */
    @Bean
    public TenantLineHandler tenantLineHandler(TenantProperties tenantProperties) {
        return new TenantHandler(tenantProperties);
    }
}
