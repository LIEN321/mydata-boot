package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.DataDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;

import java.util.List;

/**
 * 标准数据 Service接口
 *
 * @author LIEN
 * @since 2024/11/09
 */
public interface IDataService extends IBaseService<Data> {
    /**
     * 保存标准数据
     *
     * @param dataDTO 标准数据
     * @return id
     */
    Long saveData(DataDTO dataDTO);

    /**
     * 根据项目查询标准数据列表
     *
     * @param projectId 项目id
     * @return 标准数据列表
     */
    List<Data> listByProject(Long projectId);
}
