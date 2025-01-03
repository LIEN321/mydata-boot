package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import tech.zhiwei.frostmetal.core.base.common.PageParam;
import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataFilter;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;

import java.util.List;
import java.util.Map;

/**
 * 业务数据 服务类
 *
 * @author LIEN
 * @since 2024/12/05
 */
public interface IBizDataService extends IBaseService<Data> {

    /**
     * 获取数据项的总数
     *
     * @param dbCode   业务数据库名
     * @param dataCode 业务数据表名
     * @return 总数
     */
    long getTotalCount(String dbCode, String dataCode, List<BizDataFilter> bizDataFilters);

    /**
     * 更新指定标准数据的业务数据总量
     *
     * @param dataId 标准数据id
     */
    void updateDataCount(Long dataId);

    /**
     * 分页查询 业务数据
     *
     * @param pageParam 分页
     * @param dataId    数据标准id
     * @return 数据列表
     */
    IPage<Map<String, Object>> bizDataPage(PageParam pageParam, Long dataId, Map<String, Object> params);
}
