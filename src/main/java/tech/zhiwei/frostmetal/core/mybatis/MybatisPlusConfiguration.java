package tech.zhiwei.frostmetal.core.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.zhiwei.frostmetal.core.jackson.IntegerArrayTypeHandler;
import tech.zhiwei.frostmetal.core.jackson.StringArrayTypeHandler;
import tech.zhiwei.frostmetal.core.tenant.handler.TenantHandler;

/**
 * mybatis-plus 配置类
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Configuration
@MapperScan("tech.zhiwei.**.mapper")
public class MybatisPlusConfiguration {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(TenantHandler tenantHandler) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 多租户拦截器
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(tenantHandler);
        interceptor.addInnerInterceptor(tenantInterceptor);
        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.getTypeHandlerRegistry().register(StringArrayTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(IntegerArrayTypeHandler.class);
        };
    }
}
