package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.common.constant.MdConstant;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.mydata.manage.dto.DataFieldDTO;
import org.springblade.modules.mydata.manage.entity.DataField;
import org.springblade.modules.mydata.manage.mapper.DataFieldMapper;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.vo.DataFieldVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 标准数据项字段 服务实现类
 *
 * @author LIEN
 * @since 2022-07-09
 */
@Service
public class DataFieldServiceImpl extends BaseServiceImpl<DataFieldMapper, DataField> implements IDataFieldService {

    @Override
    public IPage<DataFieldVO> selectDataFieldPage(IPage<DataFieldVO> page, DataFieldVO dataField) {
        return page.setRecords(baseMapper.selectDataFieldPage(page, dataField));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveByStandardData(Long dataId, List<DataFieldDTO> dataFieldDTOList) {
        Assert.notNull(dataId);

        // 删除原有字段
        remove(Wrappers.<DataField>lambdaUpdate().eq(DataField::getDataId, dataId));
        // 保存数据项字段
        List<DataField> dataFieldList = BeanUtil.copyToList(dataFieldDTOList, DataField.class);
        if (CollUtil.isNotEmpty(dataFieldList)) {
            Set<String> fieldCodeSet = CollUtil.newHashSet();
            dataFieldList.forEach(dataField -> {
                Assert.isFalse(fieldCodeSet.contains(dataField.getFieldCode()), "提交失败：字段编号 {} 不能重复！", dataField.getFieldCode());
                dataField.setDataId(dataId);
                fieldCodeSet.add(dataField.getFieldCode());
            });
            return saveBatch(dataFieldList);
        }
        return true;
    }

    @Override
    public List<DataField> findByData(Long dataId) {
        Assert.notNull(dataId);
        return this.list(Wrappers.<DataField>lambdaQuery().eq(DataField::getDataId, dataId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByStandardData(Long dataId) {
        remove(Wrappers.<DataField>lambdaUpdate().eq(DataField::getDataId, dataId));
    }

    @Override
    public List<DataField> findIdFields(Long dataId) {
        Assert.notNull(dataId);

        return this.list(Wrappers.<DataField>lambdaQuery()
                                 .eq(DataField::getDataId, dataId).eq(DataField::getIsId, MdConstant.IS_ID_FIELD));

    }
}
