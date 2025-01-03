package tech.zhiwei.frostmetal.core.tenant.handler;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.tenant.config.TenantProperties;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;
import tech.zhiwei.frostmetal.core.tenant.entity.TreeTenantEntity;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.util.ClassUtil;

import java.util.Collection;
import java.util.Set;

/**
 * 多租户的处理类
 *
 * @author LIEN
 * @since 2024/11/2
 */
@Component
@RequiredArgsConstructor
public class TenantHandler implements TenantLineHandler, SmartInitializingSingleton {
    /**
     * 忽略多租户的表名，高优先级
     */
    private final Set<String> ignoreTables = CollectionUtil.newHashSet("sys_tenant");

    /**
     * 启动多租户的表名，低优先级
     */
    private final Set<String> tenantTables = CollectionUtil.newHashSet();

    /**
     * 多租户配置
     */
    private final TenantProperties tenantProperties;

    @Override
    public Expression getTenantId() {
        return new StringValue(AuthUtil.getTenantId());
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 符合以下条件，则忽略租户
        // 用户未登录
        return StringUtil.isBlank(AuthUtil.getTenantId())
                // 在忽略清单中
                || ignoreTables.contains(tableName)
                // 不在启用的清单中
                || !tenantTables.contains(tableName);
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (CollectionUtil.isNotEmpty(tenantProperties.getIgnoreTables())) {
            ignoreTables.addAll(tenantProperties.getIgnoreTables());
        }

        // 扫描继承 TenantEntity、TreeTenantEntity 的类，并获取TableName注解值 作为启用租户的表名
        Set<Class<?>> tenantClasses = ClassUtil.scanPackageBySuper("tech.zhiwei.frostmetal", TenantEntity.class);
        Set<Class<?>> treeTenantClasses = ClassUtil.scanPackageBySuper("tech.zhiwei.frostmetal", TreeTenantEntity.class);

        Collection<Class<?>> classes = CollectionUtil.union(tenantClasses, treeTenantClasses);
        if (CollectionUtil.isNotEmpty(classes)) {
            classes.forEach(clazz -> {
                if (clazz.isAnnotationPresent(TableName.class)) {
                    // 获取 TableName 注解值
                    TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
                    String tableName = tableNameAnnotation.value();
                    if (StringUtil.isNotEmpty(tableName) && !ignoreTables.contains(tableName)) {
                        tenantTables.add(tableName);
                    }
                }
            });
        }
    }
}
