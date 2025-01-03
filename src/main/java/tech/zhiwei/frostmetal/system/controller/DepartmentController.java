package tech.zhiwei.frostmetal.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.system.cache.SysCache;
import tech.zhiwei.frostmetal.system.dto.DepartmentDTO;
import tech.zhiwei.frostmetal.system.entity.Department;
import tech.zhiwei.frostmetal.system.service.IDepartmentService;
import tech.zhiwei.frostmetal.system.vo.DepartmentTreeVO;
import tech.zhiwei.frostmetal.system.vo.DepartmentVO;
import tech.zhiwei.frostmetal.system.wrapper.DepartmentWrapper;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.tree.TreeUtil;

import java.util.Collection;
import java.util.List;

/**
 * 机构部门管理 Controller
 *
 * @author LIEN
 * @since 2024/8/27
 */
@RestController
@RequestMapping("/department")
@AllArgsConstructor
@Tag(name = "department", description = "机构部门管理API")
public class DepartmentController {
    private IDepartmentService departmentService;

    @PostMapping
    @Operation(summary = "新增或更新部门", operationId = "saveDepartment")
    public R<Long> save(@RequestBody DepartmentDTO departmentDTO) {
        try {
            return R.data(departmentService.saveDepartment(departmentDTO));
        } finally {
            SysCache.removeDepartmentTree();
        }
    }

    @GetMapping("/list")
    @Operation(summary = "查询部门", operationId = "departmentList")
    public R<List<DepartmentVO>> list(@RequestParam(required = false) String name) {
        LambdaQueryWrapper<Department> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtil.isNotEmpty(name), Department::getName, name);
        List<DepartmentVO> list = DepartmentWrapper.getInstance().listVO(departmentService.list(queryWrapper));
        return R.data(TreeUtil.convert(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "机构部门详情", operationId = "departmentDetail")
    @Parameter(name = "id", description = "记录id")
    public R<DepartmentVO> detail(@PathVariable Long id) {
        return R.data(DepartmentWrapper.getInstance().entityVO(departmentService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除机构部门", operationId = "deleteDepartment")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            return R.status(departmentService.removeDepartment(id));
        } finally {
            SysCache.removeDepartmentTree();
        }
    }

    @DeleteMapping
    @Operation(summary = "批量删除机构部门", operationId = "deleteDepartments")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        try {
            AssertUtil.notEmpty(ids, "请选择要删除的记录");
            ids.forEach(id -> {
                departmentService.removeDepartment(id);
            });
            return R.status(true);
        } finally {
            SysCache.removeDepartmentTree();
        }
    }

    @GetMapping("/tree")
    @Operation(summary = "机构部门目录树", operationId = "departmentTree")
    public List<DepartmentTreeVO> departmentTree() {
        // 先尝试从缓存获取，若有效则直接返回
        List<DepartmentTreeVO> departmentTreeList = SysCache.getDepartmentTree();
        if (departmentTreeList != null) {
            return departmentTreeList;
        }

        List<Department> list = departmentService.list();
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }

        departmentTreeList = DepartmentWrapper.getInstance().departmentTreeVOList(list);
        departmentTreeList = TreeUtil.convert(departmentTreeList);

        // 存入缓存
        SysCache.putDepartmentTree(departmentTreeList);
        return departmentTreeList;
    }
}
