package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.DataMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IBizDataService;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.lang.AssertUtil;

/**
 * 业务数据 服务实现类
 *
 * @author LIEN
 * @since 2024/12/05
 */
@Service
public class BizDataServiceImpl extends BaseService<DataMapper, Data> implements IBizDataService {

    @Resource
    private BizDataDAO bizDataDAO;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDataCount(Long dataId) {
        Data data = getById(dataId);
        AssertUtil.notNull(data);

        Project project = MyDataCache.getProject(data.getProjectId());
        AssertUtil.notNull(project);

        // 从数据仓库统计最新数量
        long total = bizDataDAO.total(MyDataUtil.getBizDbCode(data.getTenantId(), project.getProjectCode()), data.getDataCode(), null);
        data.setDataCount(total);
        updateById(data);
    }
}
