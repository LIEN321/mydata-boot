package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.vo.SelectVO;
import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.ProjectVO;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目 Wrapper
 *
 * @author LIEN
 * @since 2024/11/09
 */
public class ProjectWrapper extends BaseWrapper<Project, ProjectVO> {
    public ProjectWrapper() {
    }

    public static ProjectWrapper getInstance() {
        return new ProjectWrapper();
    }

    @Override
    public ProjectVO entityVO(Project entity) {
        return BeanUtil.copyProperties(entity, ProjectVO.class);
    }

    public List<SelectVO> selectVOList(List<Project> entityList) {
        List<SelectVO> selectVOList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(entityList)) {
            for (Project project : entityList) {
                SelectVO selectVO = new SelectVO();
                selectVO.setLabel(project.getProjectName());
                selectVO.setValue(String.valueOf(project.getId()));
                selectVOList.add(selectVO);
            }
        }
        return selectVOList;
    }
}
