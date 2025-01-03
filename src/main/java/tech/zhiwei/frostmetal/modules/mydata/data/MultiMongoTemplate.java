package tech.zhiwei.frostmetal.modules.mydata.data;

import com.mongodb.client.MongoDatabase;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * 多数据源 MultiMongoTemplate
 *
 * @author LIEN
 * @since 2024/11/25
 */
public class MultiMongoTemplate extends MongoTemplate {

    /**
     * 用来缓存当前MongoDbFactory
     */
    private static ThreadLocal<MongoDatabaseFactory> mongoDbFactoryThreadLocal;

    public MultiMongoTemplate(MongoDatabaseFactory mongoDbFactory) {
        super(mongoDbFactory);
        if (mongoDbFactoryThreadLocal == null) {
            mongoDbFactoryThreadLocal = new ThreadLocal<>();
        }
    }

    public void setMongoDbFactory(MongoDatabaseFactory factory) {
        mongoDbFactoryThreadLocal.set(factory);
    }

    public void removeMongoDbFactory() {
        mongoDbFactoryThreadLocal.remove();
    }

    @Override
    public MongoDatabase getDb() {
        return mongoDbFactoryThreadLocal.get().getMongoDatabase();
    }
}
