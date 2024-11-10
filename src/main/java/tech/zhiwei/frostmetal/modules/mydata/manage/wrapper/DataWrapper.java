package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.DataVO;
import tech.zhiwei.tool.bean.BeanUtil;

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
}
