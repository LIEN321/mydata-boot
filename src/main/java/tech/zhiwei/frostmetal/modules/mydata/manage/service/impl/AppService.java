package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.AppMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppApiService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.util.ArrayUtil;

import java.util.List;

/**
 * 应用 Service实现类
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Service
public class AppService extends BaseService<AppMapper, App> implements IAppService {

    @Resource
    private IAppApiService appApiService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveApp(AppDTO appDTO) {
        App app = BeanUtil.copyProperties(appDTO, App.class);
        saveOrUpdate(app);
        return app.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAppApiCount(Long... ids) {
        if (ArrayUtil.isEmpty(ids)) {
            return;
        }

        List<App> apps = CollectionUtil.newArrayList();
        for (Long id : ids) {
            int apiCount = (int) appApiService.countByApp(id);
            App app = new App();
            app.setId(id);
            app.setApiCount(apiCount);
            apps.add(app);
        }
        updateBatchById(apps);
    }
}
