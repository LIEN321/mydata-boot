package tech.zhiwei.frostmetal.modules.mydata.cache;

import tech.zhiwei.frostmetal.cache.CacheUtil;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppApiService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IAppService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IProjectService;
import tech.zhiwei.tool.spring.SpringUtil;

/**
 * 系统缓存
 *
 * @author LIEN
 * @since 2024/11/9
 */
public class MdCache {
    private static final IProjectService projectService;
    private static final IAppService appService;
    private static final IAppApiService apiService;
    private static final IDataService dataService;

    static {
        projectService = SpringUtil.getBean(IProjectService.class);
        appService = SpringUtil.getBean(IAppService.class);
        apiService = SpringUtil.getBean(IAppApiService.class);
        dataService = SpringUtil.getBean(IDataService.class);
    }

    // mydata模块的缓存统一前缀
    private static final String MYDATA_CACHE_PREFIX = "mydata";
    // project缓存前缀
    private static final String CACHE_PROJECT = "project:id:";
    // app缓存前缀
    private static final String CACHE_APP = "app:id:";
    // app缓存前缀
    private static final String CACHE_API = "api:id:";
    // data缓存前缀
    private static final String CACHE_DATA = "data:id:";

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

    // ---------------------------------------- API 缓存 ----------------------------------------

    /**
     * 获取API
     *
     * @param id API id
     * @return API
     */
    public static AppApi getApi(Long id) {
        return CacheUtil.get(MYDATA_CACHE_PREFIX, CACHE_API, id, () -> apiService.getById(id));
    }

    /**
     * 删除API的缓存
     *
     * @param id 应用id
     */
    public static void removeApi(Long id) {
        CacheUtil.remove(MYDATA_CACHE_PREFIX, CACHE_API, id);
    }

    // ---------------------------------------- 数据标准 缓存 ----------------------------------------

    /**
     * 获取数据标准
     *
     * @param id 数据标准 id
     * @return 数据标准
     */
    public static Data getData(Long id) {
        return CacheUtil.get(MYDATA_CACHE_PREFIX, CACHE_DATA, id, () -> dataService.getById(id));
    }

    /**
     * 删除API的缓存
     *
     * @param id 应用id
     */
    public static void removeData(Long id) {
        CacheUtil.remove(MYDATA_CACHE_PREFIX, CACHE_DATA, id);
    }
}
