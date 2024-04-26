package org.springblade.modules.mydata.manage.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.entity.BizData;
import org.springblade.modules.mydata.manage.entity.Env;
import org.springblade.modules.mydata.manage.entity.Project;
import org.springblade.modules.mydata.manage.vo.BizDataVO;

/**
 * 业务数据概况包装类
 *
 * @author LIEN
 * @since 2024/4/26
 */
public class BizDataWrapper extends BaseEntityWrapper<BizData, BizDataVO> {
    public static BizDataWrapper build() {
        return new BizDataWrapper();
    }

    @Override
    public BizDataVO entityVO(BizData bizData) {
        BizDataVO bizDataVO = BeanUtil.copyProperties(bizData, BizDataVO.class);

        if (bizDataVO != null) {
            Project project = ManageCache.getProject(bizDataVO.getProjectId());
            if (project != null) {
                bizDataVO.setProjectName(project.getProjectName());
            }

            Env env = ManageCache.getEnv(bizDataVO.getEnvId());
            if (env != null) {
                bizDataVO.setEnvName(env.getEnvName());
            }
        }

        return bizDataVO;
    }
}
