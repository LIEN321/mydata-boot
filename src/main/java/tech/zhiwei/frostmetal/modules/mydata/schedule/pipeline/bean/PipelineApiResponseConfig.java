package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import lombok.Data;

/**
 * 流水线Api响应规则配置
 *
 * @author LIEN
 * @since 2025/11/3
 */
@Data
public class PipelineApiResponseConfig {
    /**
     * 是否验证响应码
     */
    private boolean isValidCode;

    /**
     * 需验证的响应码
     */
    private int codeValue;

    /**
     * 是否验证响应内容
     */
    private boolean isValidBody;

    /**
     * 响应内容的json路径
     */
    private String bodyJsonPath;

    /**
     * 需验证的内容值
     */
    private String bodyValue;

    /**
     * 默认验证响应码 200
     */
    private PipelineApiResponseConfig() {
        this.isValidCode = true;
        this.codeValue = 200;
    }

    /**
     * 验证响应码，并验证响应内容
     *
     * @param bodyJsonPath json路径
     * @param bodyValue    需验证的内容值
     */
    private PipelineApiResponseConfig(String bodyJsonPath, String bodyValue) {
        this();
        this.isValidBody = true;
        this.bodyJsonPath = bodyJsonPath;
        this.bodyValue = bodyValue;
    }

    public static PipelineApiResponseConfig defaultConfig() {
        return new PipelineApiResponseConfig();
    }

    public static PipelineApiResponseConfig validBodyValue(String bodyJsonPath, String bodyValue) {
        return new PipelineApiResponseConfig(bodyJsonPath, bodyValue);
    }
}
