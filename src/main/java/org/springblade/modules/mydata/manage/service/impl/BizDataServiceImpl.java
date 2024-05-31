package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MdUtil;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.BizDataDTO;
import org.springblade.modules.mydata.manage.entity.BizData;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.DataField;
import org.springblade.modules.mydata.manage.mapper.BizDataMapper;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.service.IDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 业务数据 服务实现类
 *
 * @author LIEN
 * @since 2022-07-08
 */
@Service
public class BizDataServiceImpl extends BaseServiceImpl<BizDataMapper, BizData> implements IBizDataService {

    @Resource
    private BizDataDAO bizDataDAO;

    @Resource
    private IDataService dataService;

    @Resource
    private IDataFieldService dataFieldService;

    @Override
    public IPage<Map<String, Object>> bizDataPage(IPage<List<Map<String, Object>>> page, BizDataDTO bizDataDTO, Map<String, Object> params) {
        // 校验参数
        Assert.notNull(bizDataDTO, "参数无效");
        Assert.notNull(bizDataDTO.getDataId(), "参数dataId无效");

        // 校验数据项是否有效
        Long dataId = bizDataDTO.getDataId();
        Data data = ManageCache.getData(dataId);
        Assert.notNull(data, "数据项不存在，dataId={}", dataId);

        // 根据分页参数 查询业务数据
        List<Map<String, Object>> dataList = bizDataDAO.page(MdUtil.getBizDbCode(data.getTenantId(), bizDataDTO.getProjectId(), bizDataDTO.getEnvId()), data.getDataCode(), (int) page.getCurrent(), (int) page.getSize(), params);
        dataList.forEach(bizData -> {
            bizData.remove(MdConstant.MONGODB_OBJECT_ID);
        });
        // 获取分页总数
        long total = getTotalCount(bizDataDTO);
        // 将 业务数据和分页参数 合并为分页结果
        IPage<Map<String, Object>> bizDataPage = new Page<>(page.getCurrent(), page.getSize(), total);
        bizDataPage.setRecords(dataList);

        return bizDataPage;
    }

    @Override
    public List<Map<String, Object>> bizDataList(BizDataDTO bizDataDTO, Map<String, Object> params) {
        // 校验参数
        Assert.notNull(bizDataDTO, "参数无效");
        Assert.notNull(bizDataDTO.getDataId(), "参数dataId无效");

        // 校验数据项是否有效
        Long dataId = bizDataDTO.getDataId();
        Data data = ManageCache.getData(dataId);
        return bizDataDAO.list(MdUtil.getBizDbCode(data.getTenantId(), bizDataDTO.getProjectId(), bizDataDTO.getEnvId()), data.getDataCode(), params);
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
    public boolean deleteById(Long dataId, Long envId, String bizId) {
        Data data = ManageCache.getData(dataId);
        bizDataDAO.remove(MdUtil.getBizDbCode(data.getTenantId(), data.getProjectId(), envId), data.getDataCode(), bizId);
        updateDataCount(data.getTenantId(), data.getProjectId(), envId, data.getId());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDataCount(String tenantId, Long projectId, Long envId, Long dataId) {
        // 从数据仓库统计最新数量
        long total = getTotalCount(tenantId, projectId, envId, dataId);
        // 查询业务数据量记录
        BizData bizData = getOne(projectId, envId, dataId);
        if (bizData == null) {
            bizData = new BizData();
            bizData.setProjectId(projectId);
            bizData.setEnvId(envId);
            bizData.setDataId(dataId);
        }
        bizData.setDataCount(total);
        saveOrUpdate(bizData);
    }

    @Override
    public List<BizData> listByData(Long dataId) {
        LambdaQueryWrapper<BizData> queryWrapper = Wrappers.<BizData>lambdaQuery()
                .eq(BizData::getDataId, dataId);
        return list(queryWrapper);
    }

    @Transactional
    @Override
    public void saveBizData(long projectId, long envId, long dataId, List<Map<String, Object>> bizDataList) {
        Assert.notEmpty(bizDataList);
        Data data = dataService.getById(dataId);
        Assert.notNull(data);
        List<DataField> dataFields = dataFieldService.findByData(data.getId());
        Assert.notEmpty(dataFields);
        List<DataField> idFields = dataFields.stream()
                .filter(field -> MdConstant.IS_ID_FIELD.equals(field.getIsId()))
                .collect(Collectors.toList());
        List<String> idFieldCodes = idFields.stream().map(DataField::getFieldCode).collect(Collectors.toList());
        // 映射 字段编号：字段类型
        Map<String, String> fieldTypeMapping = dataFields.stream()
                .collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));
        // 保存数据到数据中心
        List<Map<String, Object>> dataInsertList = CollUtil.newArrayList();
        List<Map<String, Object>> dataUpdateList = CollUtil.newArrayList();

        bizDataList.forEach(bizData -> {
            // 标识字段 键值对
            Map<String, Object> idMap = MapUtil.newHashMap();
            for (String idCode : idFieldCodes) {
                Object idFieldValue = bizData.get(idCode);
                idMap.put(idCode, idFieldValue);
            }

            // 根据唯一标识 查询业务数据
            Map<String, Object> queryData = bizDataDAO.findByIds(MdUtil.getBizDbCode(data.getTenantId(), projectId, envId), data.getDataCode(), idMap);

            if (queryData == null) {
                // 未查到数据，则新增
                queryData = bizData;
                dataInsertList.add(queryData);
            } else {
                // 查到数据
                // 检测数据 是否需要变更，若有则更新 否则不更新
                boolean isSame = true;
                Set<String> keys = bizData.keySet();
                for (String key : keys) {
                    Object produceDataValue = bizData.get(key);
                    Object queryDataValue = queryData.get(key);

                    // 将保存的数据 按最新配置的类型转换对比
                    String targetType = fieldTypeMapping.get(key);
                    produceDataValue = MdUtil.convertDataType(produceDataValue, targetType);
                    queryDataValue = MdUtil.convertDataType(queryDataValue, targetType);
                    if (!ObjectUtil.equal(produceDataValue, queryDataValue)) {
                        isSame = false;
                        break;
                    }
                }
                if (isSame) {
                    return;
                }
                queryData.putAll(bizData);
                dataUpdateList.add(queryData);
            }
        });

        // 新增数据 到 数据仓库
        if (!dataInsertList.isEmpty()) {
            bizDataDAO.insertBatch(MdUtil.getBizDbCode(data.getTenantId(), projectId, envId), data.getDataCode(), dataInsertList);
        }

        // 更新数据仓库的数据
        if (!dataUpdateList.isEmpty()) {
            dataUpdateList.forEach(bizData -> {
                Map<String, Object> idMap = MapUtil.newHashMap();
                idFieldCodes.forEach(idCode -> {
                    Object dataIdValue = bizData.get(idCode);
                    idMap.put(idCode, dataIdValue);
                });

                bizDataDAO.update(MdUtil.getBizDbCode(data.getTenantId(), projectId, envId), data.getDataCode(), idMap, bizData);
            });
        }

        updateDataCount(data.getTenantId(), projectId, envId, data.getId());
    }

    private BizData getOne(Long projectId, Long envId, Long dataId) {
        LambdaQueryWrapper<BizData> queryWrapper = Wrappers.<BizData>lambdaQuery()
                .eq(BizData::getProjectId, projectId)
                .eq(BizData::getEnvId, envId)
                .eq(BizData::getDataId, dataId);
        return getOne(queryWrapper);
    }
}
