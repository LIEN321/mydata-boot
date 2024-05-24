package org.springblade.modules.mydata.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.mydata.manage.dto.EnvDTO;
import org.springblade.modules.mydata.manage.entity.Env;
import org.springblade.modules.mydata.manage.vo.EnvVO;

import java.util.List;

/**
 * 环境配置 服务类
 *
 * @author LIEN
 * @since 2022-07-11
 */
public interface IEnvService extends BaseService<Env> {

    /**
     * 自定义分页
     *
     * @param page
     * @param env
     * @return
     */
    IPage<EnvVO> selectEnvPage(IPage<EnvVO> page, EnvVO env);

    /**
     * 新增或更新
     *
     * @param envDTO 环境信息
     * @return 操作结果，true-成功，false-失败
     */
    boolean submit(EnvDTO envDTO);

    /**
     * 删除环境
     *
     * @param ids 环境id
     * @return 操作结果，true-成功，false-失败
     */
    boolean deleteEnv(List<Long> ids);

    /**
     * 将环境的前缀地址 同步更新到关联任务的地址，并重启任务
     *
     * @param id 环境id
     * @return 操作结果，true-成功，false-失败
     */
    boolean syncTask(Long id);

    /**
     * 根据项目 查询环境列表
     *
     * @param projectId 项目id
     * @return 环境列表
     */
    List<Env> listByProject(Long projectId);

    /**
     * 项目下创建默认环境
     *
     * @param projectId   项目id
     * @param projectName 项目名称
     * @return 操作结果，true-成功，false-失败
     */
    boolean addDefaultEnv(Long projectId, String projectName);
}
