package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import org.springblade.modules.mydata.manage.dto.EnvDTO;
import org.springblade.modules.mydata.manage.entity.Env;
import org.springblade.modules.mydata.manage.mapper.EnvMapper;
import org.springblade.modules.mydata.manage.service.IEnvService;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springblade.modules.mydata.manage.vo.EnvVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 环境配置 服务实现类
 *
 * @author LIEN
 * @since 2022-07-11
 */
@Service
@AllArgsConstructor
public class EnvServiceImpl extends BaseServiceImpl<EnvMapper, Env>
        implements IEnvService {

    private final ITaskService taskService;

    @Override
    public IPage<EnvVO> selectEnvPage(IPage<EnvVO> page, EnvVO env) {
        return page.setRecords(baseMapper.selectEnvPage(page, env));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean submit(EnvDTO envDTO) {
        // 参数校验
        checkEnv(envDTO);

        // 复制提交的参数
        Env env = BeanUtil.copyProperties(envDTO, Env.class, "globalHeaders", "globalParams");

        // header参数转为k-v格式
        env.setGlobalHeaders(MdUtil.parseToKvMap(envDTO.getGlobalHeaders()));
        // param参数转为k-v格式
        env.setGlobalParams(MdUtil.parseToKvMap(envDTO.getGlobalParams()));

        return saveOrUpdate(env);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteEnv(List<Long> ids) {

        // 删除关联的任务
        ids.forEach(taskService::deleteByEnv);

        return deleteLogic(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean syncTask(Long id) {
        Assert.notNull(id, "同步失败，参数id无效");

        Env env = ManageCache.getEnv(id);
        Assert.notNull(env, "同步失败，参数id不存在，id={}", id);

        // 更新任务地址 并重启运行中的任务
        taskService.updateApiUrlByEnv(env);

        env.setSyncTaskTime(new Date());
        boolean result = updateById(env);
        if (!result) {
            throw new ServiceException("同步失败，更新环境失败，请重试！");
        }
        return true;
    }

    @Override
    public List<Env> listByProject(Long projectId) {
        LambdaQueryWrapper<Env> queryWrapper = Wrappers.<Env>lambdaQuery().eq(Env::getProjectId, projectId);
        return list(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addDefaultEnv(Long projectId, String projectName) {
        Env env = new Env();
        env.setEnvName(projectName + "-默认环境");
        env.setEnvPrefix("");
        env.setProjectId(projectId);
        env.setSort(1);
        return save(env);
    }

    private void checkEnv(EnvDTO envDTO) {
        Assert.notNull(envDTO, "提交失败：参数无效！");
        String envName = envDTO.getEnvName();
        String envPrefix = envDTO.getEnvPrefix();

        // 环境名称 不能为空
        Assert.notBlank(envName, "提交失败：环境名称 不能为空！");
        // 环境名称 长度不能超过限制
        Assert.isTrue(envName.length() <= MdConstant.MAX_NAME_LENGTH, "提交失败：环境名称 不能超过{}位！",
                MdConstant.MAX_NAME_LENGTH);

        // 环境前置路径 不能为空
        // Assert.notBlank(envPrefix, "提交失败：前置路径 不能为空！");
        if (envPrefix == null) {
            envDTO.setEnvPrefix("");
        }
        // 环境前置路径 长度不能超过限制
        Assert.isTrue(envPrefix == null || envPrefix.length() <= MdConstant.MAX_URI_LENGTH,
                "提交失败：前置路径 不能超过{}位！", MdConstant.MAX_URI_LENGTH);
    }
}
