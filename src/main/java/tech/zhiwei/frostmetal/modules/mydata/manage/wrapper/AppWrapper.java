package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.vo.SelectVO;
import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.AppVO;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用 Wrapper
 *
 * @author LIEN
 * @since 2024/11/11
 */
public class AppWrapper extends BaseWrapper<App, AppVO> {
    public AppWrapper() {
    }

    public static AppWrapper getInstance() {
        return new AppWrapper();
    }

    @Override
    public AppVO entityVO(App entity) {
        return BeanUtil.copyProperties(entity, AppVO.class);
    }

    public List<SelectVO> selectVOList(List<App> entityList) {
        List<SelectVO> selectVOList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(entityList)) {
            for (App app : entityList) {
                SelectVO selectVO = new SelectVO();
                selectVO.setLabel(app.getAppName());
                selectVO.setValue(String.valueOf(app.getId()));
                selectVOList.add(selectVO);
            }
        }
        return selectVOList;
    }
}
