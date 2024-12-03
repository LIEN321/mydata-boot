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
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppApiDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppApiService;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.AppApiVO;
import tech.zhiwei.frostmetal.modules.mydata.manage.wrapper.AppApiWrapper;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.Collection;
import java.util.List;

/**
 * 应用接口 Controller
 *
 * @author LIEN
 * @since 2024/11/11
 */
@RestController
@RequestMapping("/appApi")
@AllArgsConstructor
@Tag(name = "appApi", description = "应用接口API")
public class AppApiController {
    private IAppApiService appApiService;

    @PostMapping
    @Operation(summary = "新增或更新应用接口", operationId = "saveAppApi")
    public R<Long> save(@RequestBody AppApiDTO appApiDTO) {
        Long id = appApiService.saveAppApi(appApiDTO);
        if (id != null) {
            MyDataCache.removeApi(id);
        }
        return R.data(id);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询应用接口", operationId = "appApiPage")
    public P<List<AppApiVO>> page(@ParameterObject PageParam pageParam
            , @RequestParam(required = false) String appId
            , @RequestParam(required = false) String apiName
    ) {
        LambdaQueryWrapper<AppApi> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ObjectUtil.isNotNull(appId), AppApi::getAppId, appId);
        queryWrapper.like(ObjectUtil.isNotNull(apiName), AppApi::getApiName, apiName);

        return P.page(AppApiWrapper.getInstance().pageVO(appApiService.page(queryWrapper, pageParam)));
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有应用接口", operationId = "appApiList")
    public R<List<AppApiVO>> list() {
        return R.data(AppApiWrapper.getInstance().listVO(appApiService.list()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "应用接口详情", operationId = "appApiDetail")
    @Parameter(name = "id", description = "记录id")
    public R<AppApiVO> detail(@PathVariable Long id) {
        return R.data(AppApiWrapper.getInstance().entityVO(appApiService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除应用接口", operationId = "deleteAppApi")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        boolean result = appApiService.remove(id);
        if (result) {
            MyDataCache.removeApi(id);
        }
        return R.status(result);
    }

    @DeleteMapping
    @Operation(summary = "批量删除应用接口", operationId = "deleteAppApis")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        boolean result = appApiService.remove(ids);
        if (result) {
            ids.forEach(MyDataCache::removeApi);
        }
        return R.status(result);
    }

    @GetMapping("/select")
    @Operation(summary = "查询接口", operationId = "apiSelect")
    public List<SelectVO> select(@RequestParam(required = false) Long appId) {
        if (ObjectUtil.isNull(appId)) {
            return List.of();
        }
        return AppApiWrapper.getInstance().selectVOList(appApiService.listByApp(appId));
    }
}
