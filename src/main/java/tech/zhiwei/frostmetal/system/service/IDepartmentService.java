package tech.zhiwei.frostmetal.system.service;

import tech.zhiwei.frostmetal.core.base.service.ITreeService;
import tech.zhiwei.frostmetal.system.dto.DepartmentDTO;
import tech.zhiwei.frostmetal.system.entity.Department;

/**
 * 机构部门 Service接口
 *
 * @author LIEN
 * @since 2024/8/27
 */
public interface IDepartmentService extends ITreeService<Department> {
    /**
     * 新增或更新机构部门
     *
     * @param departmentDTO 机构部门数据
     * @return id
     */
    Long saveDepartment(DepartmentDTO departmentDTO);

    /**
     * 在指定租户下 新增部门
     *
     * @param tenantId      租户id
     * @param departmentDTO 部门信息
     * @return 部门id
     */
    Long saveTenantDepartment(String tenantId, DepartmentDTO departmentDTO);

    /**
     * 删除部门
     *
     * @param id 部门id
     * @return 操作结果，true-成功，false-失败
     */
    boolean removeDepartment(Long id);
}
