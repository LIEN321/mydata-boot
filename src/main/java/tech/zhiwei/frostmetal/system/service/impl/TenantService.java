package tech.zhiwei.frostmetal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.system.dto.DepartmentDTO;
import tech.zhiwei.frostmetal.system.dto.RoleDTO;
import tech.zhiwei.frostmetal.system.dto.TenantDTO;
import tech.zhiwei.frostmetal.system.dto.UserDTO;
import tech.zhiwei.frostmetal.system.entity.Menu;
import tech.zhiwei.frostmetal.system.entity.Tenant;
import tech.zhiwei.frostmetal.system.mapper.TenantMapper;
import tech.zhiwei.frostmetal.system.service.IDepartmentService;
import tech.zhiwei.frostmetal.system.service.IMenuService;
import tech.zhiwei.frostmetal.system.service.IRoleService;
import tech.zhiwei.frostmetal.system.service.ITenantService;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.util.RandomUtil;

import java.util.List;

/**
 * 系统租户 Service实现类
 *
 * @author LIEN
 * @since 2024/11/02
 */
@Service
@AllArgsConstructor
public class TenantService extends BaseService<TenantMapper, Tenant> implements ITenantService {

    private IRoleService roleService;
    private IDepartmentService departmentService;
    private IUserService userService;
    private IMenuService menuService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveTenant(TenantDTO tenantDTO) {
        Tenant tenant = BeanUtil.copyProperties(tenantDTO, Tenant.class);
        if (ObjectUtil.isNull(tenant.getId())) {
            // 新建租户，生成唯一的tenant_id
            tenant.setTenantId(RandomUtil.randomString(20));

            // 初始基础数据
            // 新建角色
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setCode("admin");
            roleDTO.setName("管理员");
            Long roleId = roleService.saveTenantRole(tenant.getTenantId(), roleDTO);

            // 关联默认菜单
            List<Menu> menus = menuService.listByCodes(SysConstant.DEFAULT_MENU_ID_LIST);
            if (CollectionUtil.isNotEmpty(menus)) {
                List<Long> menuIds = menus.stream().map(Menu::getId).toList();
                roleService.grant(CollectionUtil.toList(roleId), menuIds);
            }

            // 新建部门
            DepartmentDTO departmentDTO = new DepartmentDTO();
            departmentDTO.setName(tenant.getTenantName());
            departmentDTO.setType(SysConstant.DEPARTMENT_TYPE_COMPANY);
            departmentDTO.setSort(1);
            Long departmentId = departmentService.saveTenantDepartment(tenant.getTenantId(), departmentDTO);

            // 新建用户
            UserDTO userDTO = new UserDTO();
            userDTO.setCode("admin");
            userDTO.setName("admin");
            userDTO.setDepartmentId(departmentId);
            userDTO.setRoleId(roleId);
            userDTO.setLoginName("admin");
            userDTO.setLoginPassword("admin");
            Long userId = userService.saveTenantUser(tenant.getTenantId(), userDTO);
        }

        saveOrUpdate(tenant);
        return tenant.getId();
    }

    @Override
    public Tenant findByCode(String code) {
        Wrapper<Tenant> queryWrapper = Wrappers.<Tenant>lambdaQuery()
                .eq(Tenant::getTenantCode, code);
        return getOne(queryWrapper);
    }
}
