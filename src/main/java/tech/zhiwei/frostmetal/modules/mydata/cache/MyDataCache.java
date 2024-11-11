package tech.zhiwei.frostmetal.modules.mydata.cache;

import tech.zhiwei.frostmetal.cache.CacheUtil;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IProjectService;
import tech.zhiwei.tool.spring.SpringUtil;

/**
 * 系统缓存
 *
 * @author LIEN
 * @since 2024/11/9
 */
public class MyDataCache {
    private static final IProjectService projectService;
    private static final IAppService appService;

    static {
        projectService = SpringUtil.getBean(IProjectService.class);
        appService = SpringUtil.getBean(IAppService.class);
    }

    // mydata模块的缓存统一前缀
    private static final String MYDATA_CACHE_PREFIX = "mydata";
    // project缓存前缀
    private static final String CACHE_PROJECT = "project:id:";
    // app缓存前缀
    private static final String CACHE_APP = "app:id:";

    // ---------------------------------------- 项目缓存 ----------------------------------------

    /**
     * 获取项目
     *
     * @param id 项目id
     * @return 项目
     */
    public static Project getProject(Long id) {
        return CacheUtil.get(MYDATA_CACHE_PREFIX, CACHE_PROJECT, id, () -> projectService.getById(id));
    }

    /**
     * 删除项目的缓存
     *
     * @param id 项目id
     */
    public static void removeProject(Long id) {
        CacheUtil.remove(MYDATA_CACHE_PREFIX, CACHE_PROJECT, id);
    }


    // ---------------------------------------- 应用缓存 ----------------------------------------

    /**
     * 获取应用
     *
     * @param id 应用id
     * @return 应用
     */
    public static App getApp(Long id) {
        return CacheUtil.get(MYDATA_CACHE_PREFIX, CACHE_APP, id, () -> appService.getById(id));
    }

    /**
     * 删除应用的缓存
     *
     * @param id 应用id
     */
    public static void removeApp(Long id) {
        CacheUtil.remove(MYDATA_CACHE_PREFIX, CACHE_APP, id);
    }
}
