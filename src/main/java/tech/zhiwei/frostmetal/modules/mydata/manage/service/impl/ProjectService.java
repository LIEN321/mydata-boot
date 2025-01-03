package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.ProjectDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.ProjectMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IProjectService;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 项目 Service实现类
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Service
@AllArgsConstructor
public class ProjectService extends BaseService<ProjectMapper, Project> implements IProjectService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveProject(ProjectDTO projectDTO) {
        Project project = BeanUtil.copyProperties(projectDTO, Project.class);
        saveOrUpdate(project);
        return project.getId();
    }
}
