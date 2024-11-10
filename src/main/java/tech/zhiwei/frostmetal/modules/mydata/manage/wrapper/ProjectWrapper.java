package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.ProjectSelectVO;
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

	public List<ProjectSelectVO> selectVOList(List<Project> entityList) {
		List<ProjectSelectVO> selectVOList = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(entityList)) {
			for (Project project : entityList) {
				ProjectSelectVO projectSelectVO = new ProjectSelectVO();
				projectSelectVO.setLabel(project.getProjectName());
				projectSelectVO.setValue(project.getId());
				selectVOList.add(projectSelectVO);
			}
		}
		return selectVOList;
	}
}
