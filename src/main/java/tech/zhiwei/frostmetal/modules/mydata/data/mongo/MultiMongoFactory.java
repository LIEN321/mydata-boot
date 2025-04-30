package tech.zhiwei.frostmetal.modules.mydata.data.mongo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.extra.spring.SpringUtil;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.config.MydataConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源工厂类
 *
 * @author LIEN
 * @since 2024/11/25
 */
@Component
public class MultiMongoFactory {

    private final Map<String, MongoDatabaseFactory> mongoDbFactoryMap = new HashMap<>();

    private static final MydataConfiguration mydataConfig;

    static {
        mydataConfig = SpringUtil.getBean(MydataConfiguration.class);
    }

    public MongoTemplate getTemplate(String code) {

        String dbName = mydataConfig.isEncryptDb() ? MD5.create().digestHex(code) : code;

        // 查找项目对应的MongFactory
        MongoDatabaseFactory mongoDatabaseFactory = mongoDbFactoryMap.get(dbName);
        // 实例化
        if (mongoDatabaseFactory == null) {

            // 替换数据源
            String connectionString = StrUtil.format(mydataConfig.getMongodbUrl(), dbName);

            mongoDatabaseFactory = new SimpleMongoClientDatabaseFactory(connectionString);
            mongoDbFactoryMap.put(dbName, mongoDatabaseFactory);
        }

        return new MultiMongoTemplate(mongoDatabaseFactory);
    }
}
