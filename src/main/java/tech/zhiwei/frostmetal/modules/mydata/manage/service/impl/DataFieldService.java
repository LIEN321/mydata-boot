package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.IdService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.DataFieldDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.DataFieldMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.lang.AssertUtil;

import java.util.List;
import java.util.Set;

/**
 * 标准数据字段 Service实现类
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Service
@AllArgsConstructor
public class DataFieldService extends IdService<DataFieldMapper, DataField> implements IDataFieldService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveDataField(DataFieldDTO data_fieldDTO) {
        DataField data_field = BeanUtil.copyProperties(data_fieldDTO, DataField.class);
        saveOrUpdate(data_field);
        return data_field.getId();
    }

    @Override
    public List<DataField> listByData(Long dataId) {
        AssertUtil.notNull(dataId);
        return this.list(Wrappers.<DataField>lambdaQuery().eq(DataField::getDataId, dataId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveByStandardData(Long dataId, List<DataFieldDTO> dataFieldDTOList) {
        AssertUtil.notNull(dataId);

        // 删除原有字段
        remove(Wrappers.<DataField>lambdaUpdate().eq(DataField::getDataId, dataId));
        // 保存数据项字段
        List<DataField> dataFieldList = BeanUtil.copyToList(dataFieldDTOList, DataField.class);
        if (CollUtil.isNotEmpty(dataFieldList)) {
            Set<String> fieldCodeSet = CollUtil.newHashSet();
            dataFieldList.forEach(dataField -> {
                AssertUtil.isFalse(fieldCodeSet.contains(dataField.getFieldCode()), "提交失败：字段编号 {} 不能重复！", dataField.getFieldCode());
                dataField.setDataId(dataId);
                fieldCodeSet.add(dataField.getFieldCode());
            });
            return saveBatch(dataFieldList);
        }
        return true;
    }
}
