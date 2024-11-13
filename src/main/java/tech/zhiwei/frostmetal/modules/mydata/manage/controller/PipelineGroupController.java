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
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineGroupDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineGroup;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineGroupService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineGroupVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.PipelineGroupWrapper;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * 流水线分组 Controller
 *
 * @author LIEN
 * @since 2024/11/13
 */
@RestController
@RequestMapping("/pipelineGroup")
@AllArgsConstructor
@Tag(name = "pipelineGroup", description = "流水线分组API")
public class PipelineGroupController {
    private IPipelineGroupService pipelineGroupService;

    @PostMapping
    @Operation(summary = "新增或更新流水线分组", operationId = "savePipelineGroup")
    public R<Long> save(@RequestBody PipelineGroupDTO pipelineGroupDTO) {
        return R.data(pipelineGroupService.savePipelineGroup(pipelineGroupDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询流水线分组", operationId = "pipelineGroupPage")
    public P<List<PipelineGroupVO>> page(@ParameterObject PageParam pageParam
    ) {
        LambdaQueryWrapper<PipelineGroup> queryWrapper = Wrappers.lambdaQuery();

        return P.page(PipelineGroupWrapper.getInstance().pageVO(pipelineGroupService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询项目中的流水线分组", operationId = "pipelineGroupList")
    public R<List<PipelineGroupVO>> list(@RequestParam Long projectId) {
        LambdaQueryWrapper<PipelineGroup> queryWrapper = Wrappers.<PipelineGroup>lambdaQuery()
                .eq(ObjectUtil.isNotNull(projectId), PipelineGroup::getProjectId, projectId);

        return R.data(PipelineGroupWrapper.getInstance().listVO(pipelineGroupService.list(queryWrapper)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "流水线分组详情", operationId = "pipelineGroupDetail")
    @Parameter(name = "id", description = "记录id")
    public R<PipelineGroupVO> detail(@PathVariable Long id) {
        return R.data(PipelineGroupWrapper.getInstance().entityVO(pipelineGroupService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除流水线分组", operationId = "deletePipelineGroup")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(pipelineGroupService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除流水线分组", operationId = "deletePipelineGroups")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(pipelineGroupService.remove(ids));
    }
}
