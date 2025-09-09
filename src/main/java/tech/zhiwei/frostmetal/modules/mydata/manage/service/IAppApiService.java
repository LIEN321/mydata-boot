package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.AppApiDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;

import java.util.Collection;
import java.util.List;

/**
 * 应用接口 Service接口
 *
 * @author LIEN
 * @since 2024/11/11
 */
public interface IAppApiService extends IBaseService<AppApi> {
    /**
     * 保存应用接口
     *
     * @param appApiDTO 应用接口
     * @return id
     */
    Long saveAppApi(AppApiDTO appApiDTO);

    /**
     * 根据应用 查询接口列表
     *
     * @param appId 应用id
     * @return 接口列表
     */
    List<AppApi> listByApp(Long appId);

    /**
     * 根据应用 查询认证接口列表
     *
     * @param appId 应用id
     * @return 认证接口列表
     */
    List<AppApi> listAuthApiByApp(Long appId);

    /**
     * 根据应用 统计接口数量
     *
     * @param appId 应用id
     * @return 接口数量
     */
    long countByApp(Long appId);

    /**
     * 单个删除
     *
     * @param id 记录id
     * @return
     */
    boolean delete(Long id);

    /**
     * 批量删除
     *
     * @param ids 记录id集合
     * @return
     */
    boolean delete(Collection<Long> ids);
}
