package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.vo.SelectVO;
import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.DataVO;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 标准数据 Wrapper
 *
 * @author LIEN
 * @since 2024/11/09
 */
public class DataWrapper extends BaseWrapper<Data, DataVO> {
    public DataWrapper() {
    }

    public static DataWrapper getInstance() {
        return new DataWrapper();
    }

    @Override
    public DataVO entityVO(Data entity) {
        DataVO dataVO = BeanUtil.copyProperties(entity, DataVO.class);

        // 查询所属项目
        Project project = MyDataCache.getProject(entity.getProjectId());
        if (project != null) {
            dataVO.setProjectName(project.getProjectName());
        }

        return dataVO;
    }

    public List<SelectVO> selectVOList(List<Data> entityList) {
        List<SelectVO> selectVOList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(entityList)) {
            for (Data data : entityList) {
                SelectVO selectVO = new SelectVO();
                selectVO.setId(data.getId());
                selectVO.setLabel(data.getDataName());
                selectVO.setValue(String.valueOf(data.getId()));
                selectVOList.add(selectVO);
            }
        }
        return selectVOList;
    }
}
