package tech.zhiwei.frostmetal.modules.mydata.manage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.UserConfigDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.UserConfig;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IUserConfigService;
import tech.zhiwei.tool.util.NumberUtil;

/**
 * 用户的集成配置 Controller
 *
 * @author LIEN
 * @since 2024/12/23
 */
@RestController
@RequestMapping("/userConfig")
@AllArgsConstructor
@Tag(name = "userConfig", description = "用户的集成配置API")
public class UserConfigController {
    private IUserConfigService userConfigService;

    @PostMapping
    @Operation(summary = "新增或更新用户的集成配置", operationId = "saveUserConfig")
    public R<Long> save(@RequestBody String projectId) {
        UserConfigDTO userConfigDTO = new UserConfigDTO();
        userConfigDTO.setUserId(AuthUtil.getUserId());
        userConfigDTO.setLatestProjectId(NumberUtil.parseLong(projectId));
        return R.data(userConfigService.saveUserConfig(userConfigDTO));
    }

//    @GetMapping("/page")
//    @Operation(summary = "分页查询用户的集成配置", operationId = "userConfigPage")
//    public P<List<UserConfigVO>> page(@ParameterObject PageParam pageParam
//    ) {
//        LambdaQueryWrapper<UserConfig> queryWrapper = Wrappers.lambdaQuery();
//
//        IPage<UserConfig> page = new Page<>(pageParam.getCurrent(), pageParam.getPageSize());
//        return P.page(UserConfigWrapper.getInstance().pageVO(userConfigService.page(page, queryWrapper)));
//    }

//    @GetMapping("/list")
//    @Operation(summary = "查询所有用户的集成配置", operationId = "userConfigList")
//    public R<List<UserConfigVO>> list() {
//        return R.data(UserConfigWrapper.getInstance().listVO(userConfigService.list()));
//    }

//    @GetMapping
//    @Operation(summary = "用户的集成配置详情", operationId = "userConfigDetail")
//    @Parameter(name = "id", description = "记录id")
//    public R<UserConfigVO> detail() {
//        Long userId = AuthUtil.getUserId();
//        return R.data(UserConfigWrapper.getInstance().entityVO(userConfigService.getByUserId(userId)));
//    }

    @GetMapping("/latestProject")
    @Operation(summary = "最后管理的项目", operationId = "latestProject")
    public R<Long> latestProject() {
        Long userId = AuthUtil.getUserId();
        UserConfig userConfig = userConfigService.getByUserId(userId);
        Long projectId = userConfig != null ? userConfig.getLatestProjectId() : null;
        return R.data(projectId);
    }

//    @DeleteMapping("/{id}")
//    @Operation(summary = "单个删除用户的集成配置", operationId = "deleteUserConfig")
//    @Parameter(name = "id", description = "记录id")
//    public R<Boolean> delete(@PathVariable Long id) {
//        return R.status(userConfigService.removeById(id));
//    }

//    @DeleteMapping
//    @Operation(summary = "批量删除用户的集成配置", operationId = "deleteUserConfigs")
//    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
//        return R.status(userConfigService.removeByIds(ids));
//    }
}
