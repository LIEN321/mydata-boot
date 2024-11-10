package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IIdService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.DataFieldDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;

import java.util.List;

/**
 * 标准数据字段 Service接口
 *
 * @author LIEN
 * @since 2024/11/09
 */
public interface IDataFieldService extends IIdService<DataField> {
    /**
     * 保存标准数据字段
     *
     * @param data_fieldDTO 标准数据字段
     * @return id
     */
    Long saveDataField(DataFieldDTO data_fieldDTO);

    /**
     * 根据数据项 查询字段列表
     *
     * @param dataId 数据项id
     * @return 字段列表
     */
    List<DataField> findByData(Long dataId);

    /**
     * 保存数据项字段列表
     *
     * @param dataId           数据项id
     * @param dataFieldDTOList 字段列表
     * @return 操作结果，true-成功，false-失败
     */
    boolean saveByStandardData(Long dataId, List<DataFieldDTO> dataFieldDTOList);

}
