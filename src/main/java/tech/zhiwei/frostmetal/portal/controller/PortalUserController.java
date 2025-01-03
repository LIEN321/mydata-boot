package tech.zhiwei.frostmetal.portal.controller;

import cn.hutool.http.HttpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tech.zhiwei.frostmetal.auth.bean.AuthUser;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.portal.dto.ChangePasswordDTO;
import tech.zhiwei.frostmetal.portal.dto.UserInfoDTO;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.tool.io.FileUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.util.SystemUtil;
import tech.zhiwei.tool.util.UUID;

import java.io.File;
import java.io.IOException;

/**
 * 用户个人中心 Controller
 *
 * @author LIEN
 * @since 2024/8/30
 */
@RestController
@RequestMapping("/me")
@AllArgsConstructor
@Tag(name = "me", description = "个人中心API")
public class PortalUserController {

    private IUserService userService;

    @GetMapping("/info")
    @Operation(summary = "查询个人基本信息", operationId = "userInfo")
    public R<AuthUser> info() {
        return R.data(AuthUtil.getUser());
    }

    @PostMapping("/update")
    @Operation(summary = "更新个人基本信息", operationId = "updateUserInfo")
    public R<Object> update(@RequestBody UserInfoDTO userInfo) {
        Long userId = AuthUtil.getUserId();
        AssertUtil.notNull(userId, "更新失败：登录信息失效");

        userInfo.setId(userId);
        boolean result = userService.updateBaseInfo(userInfo);
        if (result) {
            // 更新登录用户缓存信息
            AuthUtil.update(userInfo.getName(), userInfo.getPhone(), userInfo.getEmail(), userInfo.getAvatar());
        }
        return R.status(result);
    }

    @PostMapping("/changePassword")
    @Operation(summary = "修改密码", operationId = "changePassword")
    public R<Object> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        String oldPassword = changePasswordDTO.getOldPassword();
        String newPassword = changePasswordDTO.getNewPassword();
        String confirmPassword = changePasswordDTO.getConfirmPassword();
        AssertUtil.notEmpty(oldPassword, "操作失败：原密码不能为空");
        AssertUtil.notEmpty(newPassword, "操作失败：新密码不能为空");
        AssertUtil.notEmpty(confirmPassword, "操作失败：确认密码不能为空");
        AssertUtil.equals(newPassword, confirmPassword, "操作失败：新密码和确认密码不一致");

        Long userId = AuthUtil.getUserId();
        return R.status(userService.changePassword(userId, oldPassword, newPassword));
    }

    @PostMapping("/uploadAvatar")
    @Operation(summary = "上传头像图片", operationId = "uploadAvatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        String userDir = SystemUtil.getUserDir();
        String imageDir = File.separator + "upload" + File.separator + "images" + File.separator + "avatar";
        File dir = new File(userDir + imageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = file.getOriginalFilename();
        String ext = FileUtil.getExt(fileName);
        File imageFile = new File(dir, UUID.randomUUID() + "." + ext);
        file.transferTo(imageFile);

        return R.data(imageFile.getName());
    }

    @GetMapping("/avatar/{fileName}")
    @Operation(summary = "获取头像图片", operationId = "getAvatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable(name = "fileName") String fileName) throws IOException {
        String userDir = SystemUtil.getUserDir();
        String imageDir = File.separator + "upload" + File.separator + "images" + File.separator + "avatar";
        File file = new File(userDir + imageDir + File.separator + fileName);
        Resource resource = new UrlResource(file.toURI());

        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok().contentType(MediaType.valueOf(HttpUtil.getMimeType(fileName))).body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
