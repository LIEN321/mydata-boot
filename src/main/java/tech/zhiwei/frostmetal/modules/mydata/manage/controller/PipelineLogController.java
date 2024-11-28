package tech.zhiwei.frostmetal.modules.mydata.manage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineLogDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineLogService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineLogVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.PipelineLogWrapper;

import java.util.Collection;
import java.util.List;

/**
 * 流水线执行日志 Controller
 *
 * @author LIEN
 * @since 2024/11/28
 */
@RestController
@RequestMapping("/pipelineLog")
@AllArgsConstructor
@Tag(name = "pipelineLog", description = "流水线执行日志API")
public class PipelineLogController {
    private IPipelineLogService pipelineLogService;

    @PostMapping
    @Operation(summary = "新增或更新流水线执行日志", operationId = "savePipelineLog")
    public R<Long> save(@RequestBody PipelineLogDTO pipelineLogDTO) {
        return R.data(pipelineLogService.savePipelineLog(pipelineLogDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询流水线执行日志", operationId = "pipelineLogPage")
    public P<List<PipelineLogVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam Long historyId
    ) {
        LambdaQueryWrapper<PipelineLog> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PipelineLog::getHistoryId, historyId);

        IPage<PipelineLog> page = new Page<>(pageParam.getCurrent(), pageParam.getPageSize());
        return P.page(PipelineLogWrapper.getInstance().pageVO(pipelineLogService.page(page, queryWrapper)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有流水线执行日志", operationId = "pipelineLogList")
    public R<List<PipelineLogVO>> list(@RequestParam Long historyId) {
        LambdaQueryWrapper<PipelineLog> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PipelineLog::getHistoryId, historyId);

        return R.data(PipelineLogWrapper.getInstance().listVO(pipelineLogService.list(queryWrapper)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "流水线执行日志详情", operationId = "pipelineLogDetail")
    @Parameter(name = "id", description = "记录id")
    public R<PipelineLogVO> detail(@PathVariable Long id) {
        return R.data(PipelineLogWrapper.getInstance().entityVO(pipelineLogService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除流水线执行日志", operationId = "deletePipelineLog")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(pipelineLogService.removeById(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除流水线执行日志", operationId = "deletePipelineLogs")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(pipelineLogService.removeByIds(ids));
    }
}
