package tech.zhiwei.frostmetal.dev.controller;

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
import tech.zhiwei.frostmetal.dev.dto.DevEntityDTO;
import tech.zhiwei.frostmetal.dev.dto.SaveDevEntityPropertiesDTO;
import tech.zhiwei.frostmetal.dev.entity.DevEntity;
import tech.zhiwei.frostmetal.dev.service.IDevEntityService;
import tech.zhiwei.frostmetal.dev.vo.DevEntityVO;
import tech.zhiwei.frostmetal.dev.wrapper.DevEntityWrapper;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Collection;
import java.util.List;

/**
 * 业务实体 Controller
 *
 * @author LIEN
 * @since 2024/9/28
 */
@RestController
@RequestMapping("/dev_entity")
@AllArgsConstructor
@Tag(name = "dev_entity", description = "业务实体API")
public class DevEntityController {
    private IDevEntityService devEntityService;

    @PostMapping
    @Operation(summary = "新增或更新业务实体", operationId = "saveDevEntity")
    public R<Long> save(@RequestBody DevEntityDTO devEntityDTO) {
        return R.data(devEntityService.saveDevEntity(devEntityDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询业务实体", operationId = "devEntityPage")
    public P<List<DevEntityVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) String code
            , @RequestParam(required = false) String name
            , @RequestParam(required = false) String tableName
    ) {
        LambdaQueryWrapper<DevEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtil.isNotEmpty(code), DevEntity::getCode, code)
                .like(StringUtil.isNotEmpty(name), DevEntity::getName, name)
                .like(StringUtil.isNotEmpty(tableName), DevEntity::getTableName, tableName);
        return P.page(DevEntityWrapper.getInstance().pageVO(devEntityService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有业务实体", operationId = "devEntityList")
    public R<List<DevEntityVO>> list() {
        return R.data(DevEntityWrapper.getInstance().listVO(devEntityService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "业务实体详情", operationId = "devEntityDetail")
    @Parameter(name = "id", description = "记录id")
    public R<DevEntityVO> detail(@PathVariable Long id) {
        return R.data(DevEntityWrapper.getInstance().entityVO(devEntityService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除业务实体", operationId = "deleteDevEntity")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(devEntityService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除业务实体", operationId = "deleteDevEntities")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(devEntityService.remove(ids));
    }

    @PostMapping("/save_property")
    @Operation(summary = "保存业务实体属性", operationId = "saveDevEntityProperty")
    public R<Boolean> saveDevEntityProperty(@RequestBody SaveDevEntityPropertiesDTO saveDevEntityPropertiesDTO) {
        devEntityService.saveDevEntityProperties(saveDevEntityPropertiesDTO);
        return R.success();
    }
}
