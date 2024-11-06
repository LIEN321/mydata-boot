package tech.zhiwei.frostmetal.core.tenant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 租户配置
 *
 * @author LIEN
 * @since 2024/11/4
 */
@Data
@Component
@ConfigurationProperties(prefix = "frostmetal.tenant")
public class TenantProperties {
    private List<String> ignoreTables = new ArrayList<>();
}

