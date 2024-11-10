package tech.zhiwei.frostmetal.modules.mydata.cache;

import tech.zhiwei.frostmetal.cache.CacheUtil;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
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

    static {
        projectService = SpringUtil.getBean(IProjectService.class);
    }

    // mydata模块的缓存统一前缀
    private static final String MYDATA_CACHE_PREFIX = "mydata";
    // project缓存前缀
    private static final String CACHE_PROJECT = "project:id:";

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
}
