package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppApiDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.AppApiMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppApiService;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 应用接口 Service实现类
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Service
@AllArgsConstructor
public class AppApiService extends BaseService<AppApiMapper, AppApi> implements IAppApiService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveAppApi(AppApiDTO appApiDTO) {
        AppApi appApi = BeanUtil.copyProperties(appApiDTO, AppApi.class);
        saveOrUpdate(appApi);
        return appApi.getId();
    }
}
