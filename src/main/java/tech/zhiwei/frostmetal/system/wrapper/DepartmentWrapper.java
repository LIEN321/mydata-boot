package tech.zhiwei.frostmetal.system.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.system.entity.Department;
import tech.zhiwei.frostmetal.system.vo.DepartmentTreeVO;
import tech.zhiwei.frostmetal.system.vo.DepartmentVO;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.List;

/**
 * 机构部门包装类
 *
 * @author LIEN
 * @since 2024/8/27
 */
public class DepartmentWrapper extends BaseWrapper<Department, DepartmentVO> {
    private DepartmentWrapper() {
    }

    public static DepartmentWrapper getInstance() {
        return new DepartmentWrapper();
    }

    @Override
    public DepartmentVO entityVO(Department entity) {
        return BeanUtil.copyProperties(entity, DepartmentVO.class);
    }

    public List<DepartmentTreeVO> departmentTreeVOList(List<Department> entityList) {
        List<DepartmentTreeVO> departmentTreeVOList = CollectionUtil.newArrayList();
        entityList.forEach(department -> {
            DepartmentTreeVO departmentTree = BeanUtil.copyProperties(department, DepartmentTreeVO.class);
            departmentTree.setValue(department.getId());
            departmentTree.setKey(department.getId());
            departmentTree.setTitle(department.getName());
            departmentTreeVOList.add(departmentTree);
        });

        return departmentTreeVOList;
    }
}
