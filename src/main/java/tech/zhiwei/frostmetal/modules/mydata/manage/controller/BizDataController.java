package tech.zhiwei.frostmetal.modules.mydata.manage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.core.base.common.P;
import tech.zhiwei.frostmetal.core.base.common.PageParam;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IBizDataService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.DataFieldVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.DataFieldWrapper;

import java.util.List;
import java.util.Map;

/**
 * 业务数据 控制器
 *
 * @author LIEN
 * @since 2024/12/05
 */
@RestController
@AllArgsConstructor
@RequestMapping("/bizData")
@Tag(name = "bizData", description = "标准数据项接口")
@Slf4j
public class BizDataController {

    private final IDataFieldService dataFieldService;

    private final IBizDataService bizDataService;

    /**
     * 根据数据项 查询字段列表
     *
     * @param dataId 数据项id
     * @return 字段列表
     */
    @GetMapping("/fieldList")
    @Operation(summary = "查询标准数据的可见字段列表", operationId = "bizDataFieldList")
    public R<List<DataFieldVO>> listDataFields(Long dataId) {
        return R.data(DataFieldWrapper.getInstance().listVO(dataFieldService.listDisplayedFields(dataId)));
    }

    @GetMapping("/bizDataPage")
    @Operation(summary = "分页查询业务数据", operationId = "bizDataPage")
    public P<List<Map<String, Object>>> list(@ParameterObject PageParam pageParam, @RequestParam Long dataId, @RequestParam Map<String, Object> params) {
        params.remove("current");
        params.remove("pageSize");
        params.remove("dataId");
        return P.page(bizDataService.bizDataPage(pageParam, dataId, params));
    }

    @GetMapping("/getBizData")
    @Operation(summary = "获取业务数据详情", operationId = "getBizData")
    public R<Map<String, Object>> getData(Long dataId, String bizDataId) {
        return R.data(bizDataService.getBizData(dataId, bizDataId));
    }

    @PutMapping("/saveBizData")
    @Operation(summary = "编辑业务数据", operationId = "saveBizData")
    public R<Boolean> saveBizData(@RequestParam Long dataId, @RequestParam String bizDataId, @RequestBody Map<String, Object> bizData) {
        bizDataService.saveBizData(dataId, bizDataId, bizData);
        return R.success();
    }

    @DeleteMapping("/deleteBizData")
    @Operation(summary = "删除业务数据", operationId = "deleteBizData")
    public R<Boolean> deleteBizData(@RequestParam Long dataId, @RequestParam String bizDataId) {
        bizDataService.deleteBizData(dataId, bizDataId);
        return R.success();
    }
}
