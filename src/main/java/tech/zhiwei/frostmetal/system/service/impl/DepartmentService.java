package tech.zhiwei.frostmetal.system.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.TreeService;
import tech.zhiwei.frostmetal.system.dto.DepartmentDTO;
import tech.zhiwei.frostmetal.system.entity.Department;
import tech.zhiwei.frostmetal.system.mapper.DepartmentMapper;
import tech.zhiwei.frostmetal.system.service.IDepartmentService;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.AssertUtil;

import java.util.List;

/**
 * 机构部门 Service实现类
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Service
@AllArgsConstructor
public class DepartmentService extends TreeService<DepartmentMapper, Department> implements IDepartmentService {

    private IUserService userService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveDepartment(DepartmentDTO departmentDTO) {
        Department department = BeanUtil.copyProperties(departmentDTO, Department.class);
        saveOrUpdate(department);
        return department.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeDepartment(Long id) {
        Department department = getById(id);

        List<Long> departmentIds = CollectionUtil.newArrayList();
        if (department != null) {
            // 检查部门是否有员工，若有则不能删除
            Long userCount = userService.countByDepartment(id);
            AssertUtil.equals(userCount, 0L, "操作失败：部门 {} 下还有员工，无法删除！", department.getName());

            departmentIds.add(id);
            // 查询所有子层级部门
            List<Department> departments = listAllByParentId(id);
            if (CollectionUtil.isNotEmpty(departments)) {
                List<Long> subMenuIds = departments.stream().map(Department::getId).toList();
                departmentIds.addAll(subMenuIds);
            }
        }
        return removeByIds(departmentIds);
    }
}
