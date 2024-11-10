package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.ProjectDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;

/**
 * 项目 Service接口
 *
 * @author LIEN
 * @since 2024/11/09
 */
public interface IProjectService extends IBaseService<Project> {
    /**
     * 保存项目
     * @param projectDTO 项目
     * @return id
     */
    Long saveProject(ProjectDTO projectDTO);
}
