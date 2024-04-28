package org.springblade.modules.mydata.manage.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MapUtil;
import org.springblade.common.util.MdUtil;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.mydata.job.service.JobVarService;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.ApiDTO;
import org.springblade.modules.mydata.manage.dto.ApiDebugDTO;
import org.springblade.modules.mydata.manage.entity.Api;
import org.springblade.modules.mydata.manage.service.IApiService;
import org.springblade.modules.mydata.manage.vo.ApiDebugVO;
import org.springblade.modules.mydata.manage.vo.ApiVO;
import org.springblade.modules.mydata.manage.wrapper.ApiWrapper;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 应用接口 控制器
 *
 * @author LIEN
 * @since 2022-07-08
 */
@RestController
@AllArgsConstructor
@RequestMapping(MdConstant.API_PREFIX_MANAGE + "/api")
@io.swagger.annotations.Api(value = "应用接口", tags = "应用接口接口")
public class ApiController extends BladeController {

    private final IApiService apiService;

    private JobVarService jobVarService;

    /**
     * 详情
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "详情", notes = "传入api")
    public R<ApiVO> detail(Api api) {
        Api detail = apiService.getOne(Condition.getQueryWrapper(api));
        return R.data(ApiWrapper.build().entityVO(detail));
    }

    /**
     * 分页 应用接口
     */
    @GetMapping("/list")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "分页", notes = "传入api")
    public R<IPage<ApiVO>> list(Api api, Query query) {
        LambdaQueryWrapper<Api> queryWrapper = Wrappers.lambdaQuery();
        if (api != null) {
            queryWrapper.like(ObjectUtil.isNotNull(api.getApiName()), Api::getApiName, api.getApiName());
        }

        IPage<Api> pages = apiService.page(Condition.getPage(query), queryWrapper);
        return R.data(ApiWrapper.build().pageVO(pages));
    }

    /**
     * 自定义分页 应用接口
     */
    @GetMapping("/page")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "分页", notes = "传入api")
    public R<IPage<ApiVO>> page(ApiVO api, Query query) {
        IPage<ApiVO> pages = apiService.selectApiPage(Condition.getPage(query), api);
        return R.data(pages);
    }

    /**
     * 新增 应用接口
     */
    @PostMapping("/save")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "新增", notes = "传入api")
    public R save(@Valid @RequestBody Api api) {
        boolean result = apiService.save(api);
        if (result) {
            ManageCache.clearApi(api.getId());
        }
        return R.status(result);
    }

    /**
     * 修改 应用接口
     */
    @PostMapping("/update")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "修改", notes = "传入api")
    public R update(@Valid @RequestBody Api api) {
        boolean result = apiService.updateById(api);
        if (result) {
            ManageCache.clearApi(api.getId());
        }
        return R.status(result);
    }

    /**
     * 新增或修改 应用接口
     */
    @PostMapping("/submit")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "新增或修改", notes = "传入api")
    public R submit(@Valid @RequestBody ApiDTO apiDTO) {
        boolean result = apiService.submit(apiDTO);
        if (result) {
            ManageCache.clearApi(apiDTO.getId());
        }
        return R.status(result);
    }


    /**
     * 删除 应用接口
     */
    @PostMapping("/remove")
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "逻辑删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        boolean result = apiService.deleteApi(Func.toLongList(ids));
        if (result) {
            ManageCache.clearApi(Func.toLongArray(ids));
        }
        return R.status(result);
    }

    /**
     * 下拉数据源
     */
    @GetMapping("/select")
    @ApiOperationSupport(order = 8)
    @ApiOperation(value = "下拉数据源", notes = "传入post")
    public R<List<ApiVO>> select(@RequestParam(required = false) Integer opType, @RequestParam(required = false) Long appId) {
        LambdaQueryWrapper<Api> queryWrapper = Wrappers.<Api>lambdaQuery()
                .eq(Api::getTenantId, SecureUtil.getTenantId())
                .eq(ObjectUtil.isNotNull(opType), Api::getOpType, opType)
                .eq(ObjectUtil.isNotNull(appId), Api::getAppId, appId);
        List<Api> list = apiService.list(queryWrapper);
        return R.data(ApiWrapper.build().listVO(list));
    }

    @PutMapping("/syncTask")
    public R syncTask(Long id) {
        boolean result = apiService.syncTask(id);
        if (result) {
            ManageCache.clearApi(id);
        }
        return R.status(result);
    }

    @PostMapping("/debug")
    public R<ApiDebugVO> debugApi(@RequestBody ApiDebugDTO apiDebugDTO) {
        Method method = Method.valueOf(apiDebugDTO.getHttpMethod());
        // 创建请求对象
        HttpRequest httpRequest = HttpUtil.createRequest(method, apiDebugDTO.getHttpUri());
        // 设置内容类型
        httpRequest.contentType(apiDebugDTO.getContentType());
        // 设置请求header，合并环境的全局header
        LinkedHashMap<String, String> headers = (LinkedHashMap<String, String>) MapUtil.union(apiDebugDTO.getGlobalHeaders(), MdUtil.parseToKvMap(apiDebugDTO.getHttpHeaders()));
        jobVarService.parseVar(headers, apiDebugDTO.getEnvId());
        httpRequest.headerMap(headers, true);
        // 设置请求参数，合并环境的全局param
        LinkedHashMap<String, Object> params = (LinkedHashMap<String, Object>) MapUtil.union(apiDebugDTO.getGlobalParams(), MdUtil.parseToKvMapObj(apiDebugDTO.getHttpParams()));
        jobVarService.parseVar(params, apiDebugDTO.getEnvId());
        httpRequest.form(params);

        // 记录开始时间
        long beginTime = System.currentTimeMillis();

        // 执行请求 获取响应
        HttpResponse httpResponse = httpRequest.execute();

        // 记录结束时间
        long endTime = System.currentTimeMillis();
        // 计算响应耗时
        long time = endTime - beginTime;

        // 响应状态码
        int status = httpResponse.getStatus();
        // 响应内容
        String body = httpResponse.body();

        ApiDebugVO apiDebugVO = new ApiDebugVO();
        apiDebugVO.setTime(time);
        apiDebugVO.setStatus(status);
        apiDebugVO.setBody(body);

        return R.data(apiDebugVO);
    }
}
