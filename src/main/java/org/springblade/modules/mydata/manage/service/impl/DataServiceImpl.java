package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.MdConstant;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.DataDTO;
import org.springblade.modules.mydata.manage.dto.DataStatDTO;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.Env;
import org.springblade.modules.mydata.manage.mapper.DataMapper;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.service.IDataService;
import org.springblade.modules.mydata.manage.service.IEnvService;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springblade.modules.mydata.manage.vo.DataVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标准数据项 服务实现类
 *
 * @author LIEN
 * @since 2022-07-08
 */
@Service
@AllArgsConstructor
public class DataServiceImpl extends BaseServiceImpl<DataMapper, Data> implements IDataService {

    private final IDataFieldService dataFieldService;

    private final IBizDataService bizDataService;

    private final ITaskService taskService;

    private final IEnvService envService;

    @Override
    public IPage<DataVO> selectDataPage(IPage<DataVO> page, DataVO data) {
        return page.setRecords(baseMapper.selectDataPage(page, data));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean submit(DataDTO dataDTO) {
        Assert.notNull(dataDTO, "提交失败：参数无效！");

        // 若id为空，则执行新增，否则执行更新
        if (ObjectUtil.isNull(dataDTO.getId())) {
            return save(dataDTO);
        }
        return update(dataDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return true;
        }

        ids.forEach(id -> {
            // 删除关联任务
            taskService.deleteByData(id);
            // 删除数据项字段
            dataFieldService.deleteByStandardData(id);

            Data data = ManageCache.getData(id);
            List<Env> envs = envService.listByProject(data.getProjectId());
            List<Long> envIdList = envs.stream().map(Env::getId).collect(Collectors.toList());
            // 删除业务数据
            bizDataService.deleteByEnvs(id, envIdList);
        });
        // 删除数据项
        deleteLogic(ids);

        return true;
    }

    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDataCount(String tenantId, Long dataId) {
        long total = bizDataService.getTotalCount(tenantId, dataId);
        if (total > 0) {
            Data data = new Data();
            data.setId(dataId);
            data.setDataCount(total);
            return updateById(data);
        }
        return false;
    }

    @Override
    public DataStatDTO getDataStat() {
        return baseMapper.selectDataStat();
    }

    /**
     * 新增数据项
     *
     * @param dataDTO 数据项
     * @return 操作结果，true-成功，false-失败
     */
    private boolean save(DataDTO dataDTO) {
        check(dataDTO);

        // 保存数据项
        Data data = BeanUtil.copyProperties(dataDTO, Data.class);
        boolean result = save(data);
        if (!result) {
            throw new ServiceException("新增失败！");
        }

        // 保存数据项字段
        result = dataFieldService.saveByStandardData(data.getId(), dataDTO.getDataFields());
        if (!result) {
            throw new ServiceException("新增失败！");
        }

        return true;
    }

    /**
     * 更新数据项
     *
     * @param dataDTO 数据项
     * @return 操作结果，true-成功，false-失败
     */
    private boolean update(DataDTO dataDTO) {
        check(dataDTO);

        // 不更新 数据项的编号
        dataDTO.setDataCode(null);

        // 更新数据项
        Data data = BeanUtil.copyProperties(dataDTO, Data.class);
        boolean result = updateById(data);
        if (!result) {
            throw new ServiceException("更新失败1！");
        }

        // 更新数据项字段
        result = dataFieldService.saveByStandardData(data.getId(), dataDTO.getDataFields());
        if (!result) {
            throw new ServiceException("更新失败2！");
        }
        return true;
    }

    /**
     * 根据编号查询 唯一的数据项
     *
     * @param code 编号
     * @return 数据项
     */
    private Data findByCode(String code) {
        LambdaQueryWrapper<Data> queryWrapper = Wrappers.<Data>lambdaQuery()
                .eq(Data::getDataCode, code);
        return getOne(queryWrapper);
    }

    private void check(DataDTO dataDTO) {
        // 参数校验
        Long id = dataDTO.getId();
        String dataCode = dataDTO.getDataCode();
        String dataName = dataDTO.getDataName();

        // 新增数据项 校验编号，更新操作 不支持修改编号
        if (id == null) {
            // 数据项编号 不能为空
            Assert.notBlank(dataCode, "提交失败：编号 不能为空！");
            // 数据项编号 长度不能超过限制
            Assert.isTrue(dataCode.length() <= MdConstant.MAX_CODE_LENGTH, "提交失败：编号 不能超过{}位！", MdConstant.MAX_CODE_LENGTH);

            // 校验code是否唯一
            Data check = findByCode(dataCode);
            Assert.isNull(check, "提交失败：编号 {} 已存在！", dataCode);
        }

        // 数据项名称 不能为空
        Assert.notBlank(dataName, "提交失败：名称 不能为空！");
        // 数据项名称 长度不能超过限制
        Assert.isTrue(dataName.length() <= MdConstant.MAX_NAME_LENGTH, "提交失败：名称 不能超过{}位！", MdConstant.MAX_NAME_LENGTH);
    }
}
