package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springblade.common.util.MdUtil;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.BizDataDTO;
import org.springblade.modules.mydata.manage.entity.BizData;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.mapper.BizDataMapper;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 业务数据 服务实现类
 *
 * @author LIEN
 * @since 2022-07-08
 */
@Service
@AllArgsConstructor
public class BizDataServiceImpl extends BaseServiceImpl<BizDataMapper, BizData> implements IBizDataService {

    private final BizDataDAO bizDataDAO;

    @Override
    public IPage<Map> bizDataPage(IPage<List<Map>> page, BizDataDTO bizDataDTO, Map<String, Object> params) {
        // 校验参数
        Assert.notNull(bizDataDTO, "参数无效");
        Assert.notNull(bizDataDTO.getDataId(), "参数dataId无效");

        // 校验数据项是否有效
        Long dataId = bizDataDTO.getDataId();
        Data data = ManageCache.getData(dataId);
        Assert.notNull(data, "数据项不存在，dataId={}", dataId);

        // 根据分页参数 查询业务数据
        List<Map> dataList = bizDataDAO.page(MdUtil.getBizDbCode(data.getTenantId(), bizDataDTO.getProjectId(), bizDataDTO.getEnvId()), data.getDataCode(), (int) page.getCurrent(), (int) page.getSize(), params);
        // 获取分页总数
        long total = getTotalCount(bizDataDTO);
        // 将 业务数据和分页参数 合并为分页结果
        IPage<Map> bizDataPage = new Page<>(page.getCurrent(), page.getSize(), total);
        bizDataPage.setRecords(dataList);

        return bizDataPage;
    }

    @Override
    public long getTotalCount(BizDataDTO bizDataDTO) {
        Data data = ManageCache.getData(bizDataDTO.getDataId());
        if (data == null) {
            return 0L;
        }
        return bizDataDAO.total(MdUtil.getBizDbCode(data.getTenantId(), bizDataDTO.getProjectId(), bizDataDTO.getEnvId()), data.getDataCode());
    }

    @Override
    public long getTotalCount(String tenantId, Long projectId, Long envId, Long dataId) {
        Data data = ManageCache.getData(tenantId, dataId);
        if (data == null) {
            return 0L;
        }
        return bizDataDAO.total(MdUtil.getBizDbCode(tenantId, projectId, envId), data.getDataCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByEnvs(Long dataId, List<Long> envIdList) {
        if (CollUtil.isNotEmpty(envIdList)) {
            Data data = ManageCache.getData(dataId);
            envIdList.forEach(envId -> {
                bizDataDAO.drop(MdUtil.getBizDbCode(data.getTenantId(), data.getProjectId(), envId), data.getDataCode());
                updateDataCount(data.getTenantId(), data.getProjectId(), envId, data.getId());
            });
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByEnv(Long dataId, Long envId) {
        Assert.notNull(dataId, "参数无效，dataId={}", dataId);
        Assert.notNull(envId, "参数无效，envId={}", envId);
        return deleteByEnvs(dataId, CollUtil.toList(envId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDataCount(String tenantId, Long projectId, Long envId, Long dataId) {
        // 从数据仓库统计最新数量
        long total = getTotalCount(tenantId, projectId, envId, dataId);
        // 查询业务数据量记录
        BizData bizData = getOne(projectId, envId, dataId);
        if (total > 0) {
            // 若统计结果大于0，则更新记录
            if (bizData == null) {
                bizData = new BizData();
                bizData.setProjectId(projectId);
                bizData.setEnvId(envId);
                bizData.setDataId(dataId);
            }
            bizData.setDataCount(total);
            saveOrUpdate(bizData);
        } else {
            // 统计结果没有数据，则删除记录
            if (bizData != null) {
                removeById(bizData.getId());
            }
        }
    }

    @Override
    public List<BizData> listByData(Long dataId) {
        LambdaQueryWrapper<BizData> queryWrapper = Wrappers.<BizData>lambdaQuery()
                .eq(BizData::getDataId, dataId);
        return list(queryWrapper);
    }

    private BizData getOne(Long projectId, Long envId, Long dataId) {
        LambdaQueryWrapper<BizData> queryWrapper = Wrappers.<BizData>lambdaQuery()
                .eq(BizData::getProjectId, projectId)
                .eq(BizData::getEnvId, envId)
                .eq(BizData::getDataId, dataId);
        return getOne(queryWrapper);
    }
}
