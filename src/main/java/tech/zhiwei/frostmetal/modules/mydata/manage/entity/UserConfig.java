package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.IdEntity;

/**
 * 用户的集成配置 entity
 *
 * @author LIEN
 * @since 2024/12/23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_user_config")
public class UserConfig extends IdEntity {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 最新管理的项目id
     */
    private Long latestProjectId;

}