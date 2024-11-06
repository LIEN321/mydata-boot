package tech.zhiwei.frostmetal.system.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.system.dto.SysApiDTO;
import tech.zhiwei.frostmetal.system.entity.SysApi;

/**
 * 系统接口 Service接口
 *
 * @author LIEN
 * @since 2024/9/16
 */
public interface ISysApiService extends IBaseService<SysApi> {
    /**
     * 新增或更新系统接口
     *
     * @param sysApiDTO 系统接口数据
     * @return id
     */
    Long saveSysApi(SysApiDTO sysApiDTO);

    /**
     * 根据请求信息查询接口
     *
     * @param method 请求方法
     * @param path   请求地址
     * @return 系统接口
     */
    SysApi findByRequest(String method, String path);
}
