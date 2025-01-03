package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.UserConfig;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.UserConfigVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 用户的集成配置 Wrapper
 *
 * @author LIEN
 * @since 2024/12/23
 */
public class UserConfigWrapper extends BaseWrapper<UserConfig, UserConfigVO> {
    public UserConfigWrapper() {
    }

    public static UserConfigWrapper getInstance() {
        return new UserConfigWrapper();
    }

    @Override
    public UserConfigVO entityVO(UserConfig entity) {
        return BeanUtil.copyProperties(entity, UserConfigVO.class);
    }
}
