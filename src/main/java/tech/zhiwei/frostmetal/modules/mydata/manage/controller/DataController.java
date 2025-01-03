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
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.DataDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.DataFieldVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.DataVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.DataFieldWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.DataWrapper;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * 标准数据 Controller
 *
 * @author LIEN
 * @since 2024/11/09
 */
@RestController
@RequestMapping("/data")
@AllArgsConstructor
@Tag(name = "data", description = "标准数据API")
public class DataController {
    private IDataService dataService;
    private IDataFieldService dataFieldService;

    @PostMapping
    @Operation(summary = "新增或更新标准数据", operationId = "saveData")
    public R<Long> save(@RequestBody DataDTO dataDTO) {
        return R.data(dataService.saveData(dataDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询标准数据", operationId = "dataPage")
    public P<List<DataVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) String dataCode
            , @RequestParam(required = false) String dataName
            , @RequestParam(required = false) Long projectId
    ) {
        LambdaQueryWrapper<Data> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(ObjectUtil.isNotNull(dataCode), Data::getDataCode, dataCode);
        queryWrapper.like(ObjectUtil.isNotNull(dataName), Data::getDataName, dataName);
        queryWrapper.like(ObjectUtil.isNotNull(projectId), Data::getProjectId, projectId);

        return P.page(DataWrapper.getInstance().pageVO(dataService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有标准数据", operationId = "dataList")
    public R<List<DataVO>> list() {
        return R.data(DataWrapper.getInstance().listVO(dataService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "标准数据详情", operationId = "dataDetail")
    @Parameter(name = "id", description = "记录id")
    public R<DataVO> detail(@PathVariable Long id) {
        Data data = dataService.getById(id);
        if (data == null) {
            return R.status(false);
        }

        DataVO dataVO = DataWrapper.getInstance().entityVO(data);

        List<DataFieldVO> dataFieldVOList = DataFieldWrapper.getInstance().listVO(dataFieldService.listByData(id));
        dataVO.setDataFields(dataFieldVOList);

        return R.data(dataVO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除标准数据", operationId = "deleteData")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(dataService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除标准数据", operationId = "deleteDatas")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(dataService.remove(ids));
    }

    @GetMapping("/select")
    @Operation(summary = "查询标准数据", operationId = "dataSelect")
    public List<SelectVO> select(@RequestParam Long projectId) {
        return DataWrapper.getInstance().selectVOList(dataService.listByProject(projectId));
    }

    @GetMapping("/fieldList")
    @Operation(summary = "查询标准数据的字段列表", operationId = "fieldList")
    public R<List<DataFieldVO>> fieldList(@RequestParam Long dataId) {
        return R.data(DataFieldWrapper.getInstance().listVO(dataFieldService.listByData(dataId)));
    }
}
