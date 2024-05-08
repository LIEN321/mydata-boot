package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MdUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.ApiDTO;
import org.springblade.modules.mydata.manage.dto.ApiStatDTO;
import org.springblade.modules.mydata.manage.entity.Api;
import org.springblade.modules.mydata.manage.mapper.ApiMapper;
import org.springblade.modules.mydata.manage.service.IApiService;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springblade.modules.mydata.manage.vo.ApiVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用接口 服务实现类
 *
 * @author LIEN
 * @since 2022-07-08
 */
@Service
@AllArgsConstructor
public class ApiServiceImpl extends BaseServiceImpl<ApiMapper, Api> implements IApiService {

    private final ITaskService taskService;

    @Override
    public IPage<ApiVO> selectApiPage(IPage<ApiVO> page, ApiVO api) {
        return page.setRecords(baseMapper.selectApiPage(page, api));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean submit(ApiDTO apiDTO) {
        // 参数校验
        checkApi(apiDTO);

        // 复制提交的参数
        Api api = BeanUtil.copyProperties(apiDTO, Api.class, "reqHeaders", "reqParams");

        // header参数转为k-v格式
        api.setReqHeaders(MdUtil.parseToKvMap(apiDTO.getReqHeaders()));
        // param参数转为k-v格式
        api.setReqParams(MdUtil.parseToKvMap(apiDTO.getReqParams()));

        return saveOrUpdate(api);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteApi(List<Long> ids) {
        // 删除关联的任务
        ids.forEach(taskService::deleteByApi);

        return deleteLogic(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByApp(Long appId) {
        List<Api> apiList = listByApp(appId);
        if (CollUtil.isNotEmpty(apiList)) {
            List<Long> ids = apiList.stream().map(Api::getId).collect(Collectors.toList());
            return deleteApi(ids);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean syncTask(Long id) {
        Assert.notNull(id, "同步失败，参数id无效");

        Api api = ManageCache.getApi(id);
        Assert.notNull(api, "同步失败，参数id不存在，id={}", id);

        // 更新任务 并重启运行中的任务
        taskService.updateTaskByApi(api);

        api.setSyncTaskTime(new Date());
        boolean result = updateById(api);
        if (!result) {
            throw new ServiceException("同步失败，更新API失败，请重试！");
        }
        return true;
    }

    @Override
    public ApiStatDTO getApiStat() {
        return baseMapper.selectApiStat();
    }

    /**
     * 校验参数
     *
     * @param apiDTO 待校验的参数
     */
    private void checkApi(ApiDTO apiDTO) {
        Assert.notNull(apiDTO, "提交失败：参数无效");

        // 所属应用id 不能为空
        Assert.notNull(apiDTO.getAppId(), "提交失败：所属应用无效！");

        // api名称 不能为空
        String apiName = apiDTO.getApiName();
        Assert.notBlank(apiName, "提交失败：名称 不能为空！");
        // api名称 去除前后导空格
        apiName = apiName.trim();
        // api名称 长度不能超过限制
        Assert.isTrue(apiName.length() <= MdConstant.MAX_NAME_LENGTH, "提交失败：名称 不能超过{}位！", MdConstant.MAX_NAME_LENGTH);
        apiDTO.setApiName(apiName);

        // api类型 不能为空
        Integer opType = apiDTO.getOpType();
        Assert.isTrue(MdUtil.isValidOpType(opType), "提交失败：API类型 {} 无效！", opType);

        // api方法 不能为空
        String apiMethod = apiDTO.getApiMethod();
        Assert.isTrue(MdUtil.isValidHttpMethod(apiMethod), "提交失败：请求方法 {} 无效！", apiMethod);
        // api方法 转为大写
        apiMethod = apiMethod.toUpperCase();
        apiDTO.setApiMethod(apiMethod);

        // api路径 不能为空
        String apiUri = apiDTO.getApiUri();
        Assert.notBlank(apiUri, "提交失败：路径 不能为空！");
        // api路径 去除前后导空格
        apiUri = apiUri.trim();
        // api路径 长度不能超过限制
        Assert.isTrue(apiName.length() <= MdConstant.MAX_URI_LENGTH, "提交失败：相对路径 不能超过{}位！", MdConstant.MAX_URI_LENGTH);
        apiDTO.setApiUri(apiUri);

        // 数据类型 不能为空
        String dataType = apiDTO.getDataType();
        Assert.isTrue(MdUtil.isValidDataType(dataType), "提交失败：数据类型 {} 无效！", dataType);
        // 数据类型 转为大写
        dataType = dataType.toUpperCase();
        apiDTO.setDataType(dataType);
    }

    /**
     * 根据应用查询 API列表
     *
     * @param appId 应用id
     * @return API列表
     */
    private List<Api> listByApp(Long appId) {
        LambdaQueryWrapper<Api> queryWrapper = Wrappers.<Api>lambdaQuery()
                .eq(Api::getAppId, appId);

        return list(queryWrapper);
    }
}
