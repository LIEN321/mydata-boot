package tech.zhiwei.frostmetal.modules.mydata.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Mydata 配置
 *
 * @author LIEN
 * @since 2024/11/25
 */
@Configuration
@Data
public class MydataConfiguration {
    /**
     * 是否启用 加密业务数据库名
     */
    @Value("${mydata.secure.encrypt-db:false}")
    private boolean isEncryptDb;

    /**
     * 获取配置文件的副本集连接
     */
    @Value("${mydata.mongodb.url}")
    private String mongodbUrl;

    @Value("${mydata.pipeline.max-failure-count:5}")
    private int pipelineMaxFailureCount;

    private int pipelineRetryMinCount = 0;

    @Value("${mydata.pipeline.retry-max-count:10}")
    private int pipelineRetryMaxCount;
}
