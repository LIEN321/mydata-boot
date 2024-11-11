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
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.AppVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.AppWrapper;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * 应用 Controller
 *
 * @author LIEN
 * @since 2024/11/11
 */
@RestController
@RequestMapping("/app")
@AllArgsConstructor
@Tag(name = "app", description = "应用API")
public class AppController {
    private IAppService appService;

    @PostMapping
    @Operation(summary = "新增或更新应用", operationId = "saveApp")
    public R<Long> save(@RequestBody AppDTO appDTO) {
        return R.data(appService.saveApp(appDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询应用", operationId = "appPage")
    public P<List<AppVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) String appCode
            , @RequestParam(required = false) String appName
            , @RequestParam(required = false) String appUrl
    ) {
        LambdaQueryWrapper<App> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(ObjectUtil.isNotNull(appCode), App::getAppCode, appCode);
        queryWrapper.like(ObjectUtil.isNotNull(appName), App::getAppName, appName);
        queryWrapper.like(ObjectUtil.isNotNull(appUrl), App::getAppUrl, appUrl);

        return P.page(AppWrapper.getInstance().pageVO(appService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有应用", operationId = "appList")
    public R<List<AppVO>> list() {
        return R.data(AppWrapper.getInstance().listVO(appService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "应用详情", operationId = "appDetail")
    @Parameter(name = "id", description = "记录id")
    public R<AppVO> detail(@PathVariable Long id) {
        return R.data(AppWrapper.getInstance().entityVO(appService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除应用", operationId = "deleteApp")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.status(appService.remove(id));
    }

    @DeleteMapping
    @Operation(summary = "批量删除应用", operationId = "deleteApps")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        return R.status(appService.remove(ids));
    }

    @GetMapping("/select")
    @Operation(summary = "查询所有应用", operationId = "appSelect")
    public List<SelectVO> select() {
        return AppWrapper.getInstance().selectVOList(appService.list());
    }
}
