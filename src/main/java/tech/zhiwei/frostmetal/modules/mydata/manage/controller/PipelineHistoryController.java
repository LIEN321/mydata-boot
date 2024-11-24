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
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineHistoryDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineHistoryService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineHistoryVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.PipelineHistoryWrapper;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * 流水线执行记录 Controller
 *
 * @author LIEN
 * @since 2024/11/24
 */
@RestController
@RequestMapping("/pipelineHistory")
@AllArgsConstructor
@Tag(name = "pipelineHistory", description = "流水线执行记录API")
public class PipelineHistoryController {
    private IPipelineHistoryService pipelineHistoryService;

    @PostMapping
    @Operation(summary = "新增或更新流水线执行记录", operationId = "savePipelineHistory")
    public R<Long> save(@RequestBody PipelineHistoryDTO pipelineHistoryDTO) {
        return R.data(pipelineHistoryService.savePipelineHistory(pipelineHistoryDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询流水线执行记录", operationId = "pipelineHistoryPage")
    public P<List<PipelineHistoryVO>> page(@ParameterObject PageParam pageParam
    ) {
        LambdaQueryWrapper<PipelineHistory> queryWrapper = Wrappers.lambdaQuery();

        return P.page(PipelineHistoryWrapper.getInstance().pageVO(pipelineHistoryService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有流水线执行记录", operationId = "pipelineHistoryList")
    public R<List<PipelineHistoryVO>> list() {
        return R.data(PipelineHistoryWrapper.getInstance().listVO(pipelineHistoryService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "流水线执行记录详情", operationId = "pipelineHistoryDetail")
    @Parameter(name = "id", description = "记录id")
    public R<PipelineHistoryVO> detail(@PathVariable Long id) {
        return R.data(PipelineHistoryWrapper.getInstance().entityVO(pipelineHistoryService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除流水线执行记录", operationId = "deletePipelineHistory")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(pipelineHistoryService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除流水线执行记录", operationId = "deletePipelineHistorys")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(pipelineHistoryService.remove(ids));
    }
}
