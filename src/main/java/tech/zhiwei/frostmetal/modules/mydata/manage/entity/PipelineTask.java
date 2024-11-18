package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;
import java.util.Map;

/**
 * 流水线任务 entity
 *
 * @author LIEN
 * @since 2024/11/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_pipeline_task", autoResultMap = true)
public class PipelineTask extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 5167132195306450765L;

    /**
     * 所属流水线
     */
    private Long pipelineId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 关联应用
     */
    private Long appId;

    /**
     * 关联API
     */
    private Long apiId;

    /**
     * 关联数据
     */
    private Long dataId;

    /**
     * 任务配置
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> taskConfig;

}