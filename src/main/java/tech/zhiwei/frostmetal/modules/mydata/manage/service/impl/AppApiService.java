package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppApiDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.AppApiMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppApiService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.util.ArrayUtil;

import java.util.Collection;
import java.util.List;

/**
 * 应用接口 Service实现类
 *
 * @author LIEN
 * @since 2024/11/11
 */
@Service
public class AppApiService extends BaseService<AppApiMapper, AppApi> implements IAppApiService {

    @Lazy
    @Resource
    private IAppService appService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveAppApi(AppApiDTO appApiDTO) {
        AppApi appApi = BeanUtil.copyProperties(appApiDTO, AppApi.class);
        saveOrUpdate(appApi);
        appService.updateAppApiCount(appApi.getAppId());
        return appApi.getId();
    }

    @Override
    public List<AppApi> listByApp(Long appId) {
        LambdaQueryWrapper<AppApi> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AppApi::getAppId, appId);
        return list(queryWrapper);
    }

    @Override
    public List<AppApi> listAuthApiByApp(Long appId) {
        LambdaQueryWrapper<AppApi> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AppApi::getAppId, appId);
        queryWrapper.eq(AppApi::getOpType, MyDataConstant.API_TYPE_APP_AUTH);
        return list(queryWrapper);
    }

    @Override
    public long countByApp(Long appId) {
        LambdaQueryWrapper<AppApi> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(AppApi::getAppId, appId);
        return count(queryWrapper);
    }

    @Override
    public boolean delete(Long id) {
        AppApi appApi = MyDataCache.getApi(id);
        if (appApi != null) {
            Long appId = appApi.getAppId();
            remove(id);
            appService.updateAppApiCount(appId);
            return true;
        }

        return false;
    }

    @Override
    public boolean delete(Collection<Long> ids) {
        List<AppApi> list = listByIds(ids);
        if (CollectionUtil.isNotEmpty(list)) {
            remove(ids);

            Long[] appIds = list.stream().map(AppApi::getAppId).distinct().toArray(Long[]::new);
            if (ArrayUtil.isNotEmpty(appIds)) {
                appService.updateAppApiCount(appIds);
            }
            return true;
        }
        return false;
    }
}
