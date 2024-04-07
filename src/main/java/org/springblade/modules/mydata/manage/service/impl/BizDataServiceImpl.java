package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springblade.common.util.MdUtil;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.BizDataDTO;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 标准数据项 服务实现类
 *
 * @author LIEN
 * @since 2022-07-08
 */
@Service
@AllArgsConstructor
public class BizDataServiceImpl implements IBizDataService {

    private final BizDataDAO bizDataDAO;

    @Override
    public IPage<Map> bizDataPage(IPage<List<Map>> page, BizDataDTO bizDataDTO) {
        // 校验参数
        Assert.notNull(bizDataDTO, "参数无效");
        Assert.notNull(bizDataDTO.getDataId(), "参数dataId无效");

        // 校验数据项是否有效
        Long dataId = bizDataDTO.getDataId();
        Data data = ManageCache.getData(dataId);
        Assert.notNull(data, "数据项不存在，dataId={}", dataId);

        // 根据分页参数 查询业务数据
        List<Map> dataList = bizDataDAO.page(MdUtil.getBizDbCode(data.getTenantId(), bizDataDTO.getProjectId(), bizDataDTO.getEnvId()), data.getDataCode(), (int) page.getCurrent(), (int) page.getSize());
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
    public long getTotalCount(String tenantId, Long dataId) {
        Data data = ManageCache.getData(tenantId, dataId);
        if (data == null) {
            return 0L;
        }
        return bizDataDAO.total(data.getTenantId(), data.getDataCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByEnvs(Long dataId, List<Long> envIdList) {
        if (CollUtil.isNotEmpty(envIdList)) {
            Data data = ManageCache.getData(dataId);
            envIdList.forEach(envId -> {
                bizDataDAO.drop(MdUtil.getBizDbCode(data.getTenantId(), data.getProjectId(), envId), data.getDataCode());
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
}
