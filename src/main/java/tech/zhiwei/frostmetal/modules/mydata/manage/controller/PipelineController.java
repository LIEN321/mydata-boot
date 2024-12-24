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
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineHistoryService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineTaskVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.PipelineTaskWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.PipelineWrapper;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineScheduler;

import java.util.Collection;
import java.util.List;

/**
 * 流水线 Controller
 *
 * @author LIEN
 * @since 2024/11/14
 */
@RestController
@RequestMapping("/pipeline")
@AllArgsConstructor
@Tag(name = "pipeline", description = "流水线API")
public class PipelineController {
    private IPipelineService pipelineService;
    private IPipelineTaskService pipelineTaskService;
    private IPipelineHistoryService pipelineHistoryService;
    private PipelineScheduler pipelineScheduler;

    @PostMapping
    @Operation(summary = "新增或更新流水线", operationId = "savePipeline")
    public R<Long> save(@RequestBody PipelineDTO pipelineDTO) {
        Long id = pipelineService.savePipeline(pipelineDTO);

        // 调整调度
        pipelineScheduler.update(id);

        return R.data(id);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询流水线", operationId = "pipelinePage")
    public P<List<PipelineVO>> page(@ParameterObject PageParam pageParam
    ) {
        LambdaQueryWrapper<Pipeline> queryWrapper = Wrappers.lambdaQuery();

        return P.page(PipelineWrapper.getInstance().pageVO(pipelineService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有流水线", operationId = "pipelineList")
    public R<List<PipelineVO>> list() {
        return R.data(PipelineWrapper.getInstance().listVO(pipelineService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "流水线详情", operationId = "pipelineDetail")
    @Parameter(name = "id", description = "记录id")
    public R<PipelineVO> detail(@PathVariable Long id) {
        PipelineVO pipelineVO = PipelineWrapper.getInstance().entityVO(pipelineService.getById(id));

        // 查询流水线的任务列表
        List<PipelineTaskVO> pipelineTaskVOList = PipelineTaskWrapper.getInstance().listVO(pipelineTaskService.listByPipeline(id));
        pipelineVO.setTasks(pipelineTaskVOList);

        return R.data(pipelineVO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除流水线", operationId = "deletePipeline")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(pipelineService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除流水线", operationId = "deletePipelines")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(pipelineService.remove(ids));
    }

    @GetMapping("/execute/{id}")
    @Operation(summary = "执行流水线", operationId = "executePipeline")
    public R<Boolean> execute(@PathVariable Long id) {
        PipelineHistory pipelineHistory = pipelineHistoryService.latestHistory(id);
        if (pipelineHistory != null
                && MyDataConstant.PIPELINE_HISTORY_STATUS_RUNNING == pipelineHistory.getExecutionStatus()
                && MyDataConstant.JOB_TRIGGER_TYPE_MANUAL.equals(pipelineHistory.getTriggerType())
        ) {
            return R.fail("执行失败：流水线正在运行中，请稍后再试！");
        }

        pipelineScheduler.executePipeline(id);
        return R.success();
    }

    @GetMapping("/stop/{id}")
    @Operation(summary = "停止流水线", operationId = "stopPipeline")
    public R<Boolean> stop(@PathVariable Long id) {
        PipelineHistory pipelineHistory = pipelineHistoryService.latestHistory(id);
        if (pipelineHistory != null && MyDataConstant.PIPELINE_HISTORY_STATUS_RUNNING != pipelineHistory.getExecutionStatus()) {
            return R.fail("停止失败：流水线不在运行中，请确认！");
        }

        pipelineScheduler.stopPipeline(id);
        return R.success();
    }

    @GetMapping("/select")
    @Operation(summary = "查询流水线", operationId = "pipelineSelect")
    public List<SelectVO> select(@RequestParam Long projectId) {
        return PipelineWrapper.getInstance().selectVOList(pipelineService.listByProject(projectId));
    }
}
