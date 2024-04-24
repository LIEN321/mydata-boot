package org.springblade.modules.mydata.manage.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.Project;
import org.springblade.modules.mydata.manage.vo.DataVO;

/**
 * 标准数据项包装类,返回视图层所需的字段
 *
 * @author LIEN
 * @since 2022-07-08
 */
public class DataWrapper extends BaseEntityWrapper<Data, DataVO> {

    public static DataWrapper build() {
        return new DataWrapper();
    }

    @Override
    public DataVO entityVO(Data data) {
        DataVO dataVO = BeanUtil.copy(data, DataVO.class);
        Project project = ManageCache.getProject(data.getProjectId());
        if (project != null) {
            dataVO.setProjectName(project.getProjectName());
        }
        return dataVO;
    }

}
