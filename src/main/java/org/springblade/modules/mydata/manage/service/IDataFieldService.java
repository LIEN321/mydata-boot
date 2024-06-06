package org.springblade.modules.mydata.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.mydata.manage.dto.DataFieldDTO;
import org.springblade.modules.mydata.manage.entity.DataField;
import org.springblade.modules.mydata.manage.vo.DataFieldVO;

import java.util.List;

/**
 * 标准数据项字段 服务类
 *
 * @author LIEN
 * @since 2022-07-09
 */
public interface IDataFieldService extends BaseService<DataField> {

    /**
     * 自定义分页
     *
     * @param page
     * @param dataField
     * @return
     */
    IPage<DataFieldVO> selectDataFieldPage(IPage<DataFieldVO> page, DataFieldVO dataField);

    /**
     * 保存数据项字段列表
     *
     * @param dataId           数据项id
     * @param dataFieldDTOList 字段列表
     * @return 操作结果，true-成功，false-失败
     */
    boolean saveByStandardData(Long dataId, List<DataFieldDTO> dataFieldDTOList);

    /**
     * 根据数据项 查询字段列表
     *
     * @param dataId 数据项id
     * @return 字段列表
     */
    List<DataField> findByData(Long dataId);

    /**
     * 根据数据项 查询可显示的字段列表
     *
     * @param dataId 数据项id
     * @return 字段列表
     */
    List<DataField> findDisplayedFields(Long dataId);

    /**
     * 删除数据项字段列表
     *
     * @param dataId 数据项id
     */
    void deleteByStandardData(Long dataId);

    /**
     * 根据数据项 查询唯一的标识字段
     *
     * @param dataId 应用接口id
     * @return 标识字段
     */
    List<DataField> findIdFields(Long dataId);
}
