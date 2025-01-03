package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.AppMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppService;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 应用 Service实现类
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Service
@AllArgsConstructor
public class AppService extends BaseService<AppMapper, App> implements IAppService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveApp(AppDTO appDTO) {
        App app = BeanUtil.copyProperties(appDTO, App.class);
        saveOrUpdate(app);
        return app.getId();
    }
}
