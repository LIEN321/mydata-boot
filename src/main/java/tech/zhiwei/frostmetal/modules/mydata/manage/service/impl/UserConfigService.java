package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.IdService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.UserConfigDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.UserConfig;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.UserConfigMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IUserConfigService;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 用户的集成配置 Service实现类
 *
 * @author LIEN
 * @since 2024/12/23
 */
@Service
@AllArgsConstructor
public class UserConfigService extends IdService<UserConfigMapper, UserConfig> implements IUserConfigService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveUserConfig(UserConfigDTO userConfigDTO) {
        UserConfig userConfig = getByUserId(userConfigDTO.getUserId());
        if (userConfig == null) {
            userConfig = new UserConfig();
        }
        BeanUtil.copyProperties(userConfigDTO, userConfig);
        saveOrUpdate(userConfig);
        return userConfig.getId();
    }

    @Override
    public UserConfig getByUserId(Long userId) {
        LambdaQueryWrapper<UserConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(UserConfig::getUserId, userId);
        return getOne(queryWrapper);
    }
}
