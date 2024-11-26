package tech.zhiwei.frostmetal.modules.mydata.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.core.base.common.P;
import tech.zhiwei.frostmetal.core.base.common.PageParam;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.core.base.vo.SelectVO;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.ProjectDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IProjectService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.ProjectVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.ProjectWrapper;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * 项目 Controller
 *
 * @author LIEN
 * @since 2024/11/09
 */
@RestController
@RequestMapping("/project")
@AllArgsConstructor
@Tag(name = "project", description = "项目API")
public class ProjectController {
    private IProjectService projectService;

    @PostMapping
    @Operation(summary = "新增或更新项目", operationId = "saveProject")
    public R<Long> save(@RequestBody ProjectDTO projectDTO) {
        Long id = projectService.saveProject(projectDTO);
        if (id != null) {
            MyDataCache.removeProject(projectDTO.getId());
        }
        return R.data(id);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询项目", operationId = "projectPage")
    public P<List<ProjectVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) String projectCode
            , @RequestParam(required = false) String projectName
    ) {
        LambdaQueryWrapper<Project> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(ObjectUtil.isNotNull(projectCode), Project::getProjectCode, projectCode);
        queryWrapper.like(ObjectUtil.isNotNull(projectName), Project::getProjectName, projectName);

        return P.page(ProjectWrapper.getInstance().pageVO(projectService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有项目", operationId = "projectList")
    public R<List<ProjectVO>> list() {
        return R.data(ProjectWrapper.getInstance().listVO(projectService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "项目详情", operationId = "projectDetail")
    @Parameter(name = "id", description = "记录id")
    public R<ProjectVO> detail(@PathVariable Long id) {
        return R.data(ProjectWrapper.getInstance().entityVO(projectService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除项目", operationId = "deleteProject")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(projectService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除项目", operationId = "deleteProjects")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(projectService.remove(ids));
    }

    @GetMapping("/select")
    @Operation(summary = "查询所有项目", operationId = "projectSelect")
    public List<SelectVO> select() {
        return ProjectWrapper.getInstance().selectVOList(projectService.list());
    }
}
