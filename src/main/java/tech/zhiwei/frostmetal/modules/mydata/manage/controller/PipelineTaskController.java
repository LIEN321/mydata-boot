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
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineTaskDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineTaskVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.PipelineTaskWrapper;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * 流水线任务 Controller
 *
 * @author LIEN
 * @since 2024/11/16
 */
@RestController
@RequestMapping("/pipelineTask")
@AllArgsConstructor
@Tag(name = "pipelineTask", description = "流水线任务API")
public class PipelineTaskController {
    private IPipelineTaskService pipelineTaskService;

    @PostMapping
    @Operation(summary = "新增或更新流水线任务", operationId = "savePipelineTask")
    public R<Long> save(@RequestBody PipelineTaskDTO pipelineTaskDTO) {
        return R.data(pipelineTaskService.savePipelineTask(pipelineTaskDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询流水线任务", operationId = "pipelineTaskPage")
    public P<List<PipelineTaskVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) String pipelineId
    ) {
        LambdaQueryWrapper<PipelineTask> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ObjectUtil.isNotNull(pipelineId), PipelineTask::getPipelineId, pipelineId);

        return P.page(PipelineTaskWrapper.getInstance().pageVO(pipelineTaskService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有流水线任务", operationId = "pipelineTaskList")
    public R<List<PipelineTaskVO>> list() {
        return R.data(PipelineTaskWrapper.getInstance().listVO(pipelineTaskService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "流水线任务详情", operationId = "pipelineTaskDetail")
    @Parameter(name = "id", description = "记录id")
    public R<PipelineTaskVO> detail(@PathVariable Long id) {
        return R.data(PipelineTaskWrapper.getInstance().entityVO(pipelineTaskService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除流水线任务", operationId = "deletePipelineTask")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(pipelineTaskService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除流水线任务", operationId = "deletePipelineTasks")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(pipelineTaskService.remove(ids));
    }
}
