package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IIdService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.UserConfigDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.UserConfig;

/**
 * 用户的集成配置 Service接口
 *
 * @author LIEN
 * @since 2024/12/23
 */
public interface IUserConfigService extends IIdService<UserConfig> {
    /**
     * 保存用户的集成配置
     *
     * @param userConfigDTO 用户的集成配置
     * @return id
     */
    Long saveUserConfig(UserConfigDTO userConfigDTO);

    /**
     * 根据用户id 获取唯一的配置
     *
     * @param userId 用户id
     * @return 配置
     */
    UserConfig getByUserId(Long userId);
}
