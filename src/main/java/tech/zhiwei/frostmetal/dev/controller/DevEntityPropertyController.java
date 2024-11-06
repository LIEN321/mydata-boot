package tech.zhiwei.frostmetal.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.dev.service.IDevEntityPropertyService;
import tech.zhiwei.frostmetal.dev.vo.DevEntityPropertyVO;
import tech.zhiwei.frostmetal.dev.wrapper.DevEntityPropertyWrapper;

import java.util.List;

/**
 * 业务实体属性 Controller
 *
 * @author LIEN
 * @since 2024/9/28
 */
@RestController
@RequestMapping("/dev_entity_property")
@AllArgsConstructor
@Tag(name = "dev_entity_property", description = "业务实体属性API")
public class DevEntityPropertyController {
    private IDevEntityPropertyService devEntityPropertyService;

    @GetMapping("/list")
    @Operation(summary = "业务实体属性列表", operationId = "devEntityPropertyList")
    @Parameter(name = "id", description = "业务实体id")
    public R<List<DevEntityPropertyVO>> listEntityProperties(@RequestParam Long id) {
        return R.data(DevEntityPropertyWrapper.getInstance().listVO(devEntityPropertyService.listByEntityId(id)));
    }
}
