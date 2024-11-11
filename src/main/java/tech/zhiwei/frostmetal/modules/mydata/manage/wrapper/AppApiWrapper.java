package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.AppApiVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 应用接口 Wrapper
 *
 * @author LIEN
 * @since 2024/11/11
 */
public class AppApiWrapper extends BaseWrapper<AppApi, AppApiVO> {
	public AppApiWrapper() {
	}

	public static AppApiWrapper getInstance() {
		return new AppApiWrapper();
	}

	@Override
	public AppApiVO entityVO(AppApi entity) {
		AppApiVO appApiVO = BeanUtil.copyProperties(entity, AppApiVO.class);

		// 查询所属应用
		App app = MyDataCache.getApp(entity.getAppId());
		if (app != null) {
			appApiVO.setAppName(app.getAppName());
		}

		return appApiVO;
	}
}
