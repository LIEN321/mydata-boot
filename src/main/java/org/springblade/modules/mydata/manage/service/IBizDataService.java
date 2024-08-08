package org.springblade.modules.mydata.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.mydata.data.BizDataFilter;
import org.springblade.modules.mydata.manage.dto.BizDataDTO;
import org.springblade.modules.mydata.manage.entity.BizData;

import java.util.List;
import java.util.Map;

/**
 * 业务数据 服务类
 *
 * @author LIEN
 * @since 2022-07-08
 */
public interface IBizDataService extends BaseService<BizData> {

    /**
     * 自定义分页
     *
     * @param page       分页
     * @param bizDataDTO 参数
     * @return 数据列表
     */
    IPage<Map<String, Object>> bizDataPage(IPage<List<Map<String, Object>>> page, BizDataDTO bizDataDTO, Map<String, Object> params);

    /**
     * 自定义分页
     *
     * @param page       分页
     * @param bizDataDTO 参数
     * @return 数据列表
     */
    IPage<Map<String, Object>> bizDataHistoryPage(IPage<List<Map<String, Object>>> page, BizDataDTO bizDataDTO, Map<String, Object> params);

    /**
     * 查询业务数据列表
     *
     * @param bizDataDTO 固定参数
     * @param params     自定义过滤条件
     * @return 业务数据列表
     */
    List<Map<String, Object>> bizDataList(BizDataDTO bizDataDTO, Map<String, Object> params);

    /**
     * 获取数据项的总数
     *
     * @param dbCode   业务数据库名
     * @param dataCode 业务数据表名
     * @return 总数
     */
    long getTotalCount(String dbCode, String dataCode, List<BizDataFilter> bizDataFilters);

    /**
     * 后台服务 更新数据项的总数
     *
     * @param tenantId  租户id
     * @param projectId 项目id
     * @param envId     环境id
     * @param dataId    数据项id
     * @return 总数
     */
    long getTotalCount(String tenantId, Long projectId, Long envId, Long dataId);

    /**
     * 删除指定数据项的业务数据
     *
     * @param dataId    数据项id
     * @param envIdList 环境id列表
     * @return 操作结果，true-成功，false-失败
     */
    boolean deleteByEnvs(Long dataId, List<Long> envIdList);

    /**
     * 删除指定环境的业务数据
     *
     * @param dataId 数据项id
     * @param envId  环境id
     * @return 操作结果，true-成功，false-失败
     */
    boolean deleteByEnv(Long dataId, Long envId);

    /**
     * 根据 数据标识 删除业务数据
     *
     * @param dataId 数据项id
     * @param envId  环境id
     * @param bizId  数据标识
     * @return 操作结果，true-成功，false-失败
     */
    boolean deleteById(Long dataId, Long envId, String bizId);

    /**
     * 更新业务数据量
     *
     * @param tenantId  租户id
     * @param projectId 项目id
     * @param envId     环境id
     * @param dataId    数据项id
     */
    void updateDataCount(String tenantId, Long projectId, Long envId, Long dataId);

    /**
     * 根据数据项 查询业务数据概况列表
     *
     * @param dataId 数据项id
     * @return 业务数据概况列表
     */
    List<BizData> listByData(Long dataId);

    /**
     * 保存业务数据
     *
     * @param projectId   项目id
     * @param envId       环境id
     * @param dataId      数据id
     * @param bizDataList 业务数据集合
     */
    void saveBizData(long projectId, long envId, long dataId, List<Map<String, Object>> bizDataList);
}
