package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.common.PageParam;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataFilter;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IBizDataService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataService;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.List;
import java.util.Map;

/**
 * 业务数据 服务实现类
 *
 * @author LIEN
 * @since 2024/12/05
 */
@Service
public class BizDataServiceImpl implements IBizDataService {

    @Resource
    private BizDataDAO bizDataDAO;
    @Resource
    private IDataService dataService;
    @Resource
    private DataFieldService dataFieldService;

    @Override
    public long getTotalCount(String dbCode, String dataCode, List<BizDataFilter> bizDataFilters) {
        return bizDataDAO.total(dbCode, dataCode, bizDataFilters);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDataCount(Long dataId) {
        Data data = dataService.getById(dataId);
        AssertUtil.notNull(data);

        Project project = MyDataCache.getProject(data.getProjectId());
        AssertUtil.notNull(project);

        // 从数据仓库统计最新数量
        long total = getTotalCount(MyDataUtil.getBizDbCode(data.getTenantId(), project.getProjectCode()), data.getDataCode(), null);
        data.setDataCount(total);
        dataService.updateById(data);
    }

    @Override
    public IPage<Map<String, Object>> bizDataPage(PageParam pageParam, Long dataId, Map<String, Object> params) {

        // 校验数据项是否有效
        Data data = MyDataCache.getData(dataId);
        AssertUtil.notNull(data, "数据标准不存在，dataId={}", dataId);

        Project project = MyDataCache.getProject(data.getProjectId());
        AssertUtil.notNull(project, "项目不存在，dataId={}", dataId);

        // 根据分页参数 查询业务数据
        String dbCode = MyDataUtil.getBizDbCode(data.getTenantId(), project.getProjectCode());
        String dataCode = data.getDataCode();
        List<BizDataFilter> bizDataFilters = CollectionUtil.toList();
        if (MapUtil.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                BizDataFilter filter = new BizDataFilter();
                filter.setType(MyDataConstant.TASK_FILTER_TYPE_VALUE);
                filter.setKey(k);
                filter.setOp(MyDataConstant.DATA_OP_LIKE);
                filter.setValue(v);
                bizDataFilters.add(filter);
            });
        }
        List<Map<String, Object>> dataList = bizDataDAO.page(dbCode, dataCode, pageParam.getCurrent(), pageParam.getPageSize(), bizDataFilters);
        dataList.forEach(bizData -> {
            bizData.remove(MyDataConstant.MONGODB_OBJECT_ID);
        });
        // 获取分页总数
        long total = getTotalCount(dbCode, dataCode, bizDataFilters);
        // 将 业务数据和分页参数 合并为分页结果
        IPage<Map<String, Object>> bizDataPage = new Page<>(pageParam.getCurrent(), pageParam.getPageSize(), total);
        bizDataPage.setRecords(dataList);

        return bizDataPage;
    }

    @Override
    public Map<String, Object> getBizData(Long dataId, String bizDataId) {
        Data data = MyDataCache.getData(dataId);
        Project project = MyDataCache.getProject(data.getProjectId());
        // 根据分页参数 查询业务数据
        String dbCode = MyDataUtil.getBizDbCode(data.getTenantId(), project.getProjectCode());
        String dataCode = data.getDataCode();

        return bizDataDAO.findByMdId(dbCode, dataCode, bizDataId);
    }

    @Override
    public void saveBizData(Long dataId, String bizDataId, Map<String, Object> bizData) {
        AssertUtil.notEmpty(bizDataId);

        // 获取数据标准
        Data data = MyDataCache.getData(dataId);
        AssertUtil.notNull(data);

        // 获取所属项目
        Project project = MyDataCache.getProject(data.getProjectId());
        AssertUtil.notNull(project);

        // 标识字段 键值对
        Map<String, Object> idMap = MapUtil.newHashMap();
        idMap.put(MyDataConstant.DATA_COLUMN_DATA_ID, bizDataId);

        // 根据唯一标识 查询业务数据
        bizDataDAO.update(MyDataUtil.getBizDbCode(data.getTenantId(), project.getProjectCode()), data.getDataCode(), idMap, bizData);
    }

    @Override
    public void deleteBizData(Long dataId, String bizDataId) {
        AssertUtil.notEmpty(bizDataId);

        // 获取数据标准
        Data data = MyDataCache.getData(dataId);
        AssertUtil.notNull(data);

        // 获取所属项目
        Project project = MyDataCache.getProject(data.getProjectId());
        AssertUtil.notNull(project);

        bizDataDAO.remove(MyDataUtil.getBizDbCode(data.getTenantId(), project.getProjectCode()), data.getDataCode(), bizDataId);
    }
}
