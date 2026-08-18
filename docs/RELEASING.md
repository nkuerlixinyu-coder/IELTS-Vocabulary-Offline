# 发布检查清单

本文件用于准备 GitHub 仓库公开发布、网页版 ZIP 和 Android APK Release。版权声明不能替代第三方内容的实际授权或其他合法依据。

## 公开仓库前

- [ ] 确认仓库所有者愿意以 `LICENSE` 中的 MIT 条款开放其原创程序代码。
- [x] `LICENSE` 已使用稳定的 GitHub 身份 `nkuerlixinyu-coder` 作为原创代码许可人。
- [ ] 逐项确认教材文字、音频、卡片图片和原页图片是否有权公开分发；不能确认的内容应先移除。
- [ ] 确认 `COPYRIGHT.md` 的权利人联系与删除流程可实际执行，并启用 GitHub Issues。
- [ ] 检查提交历史中没有签名密钥、账号、令牌、本机路径或其他敏感信息。
- [ ] 确认 `android-app/local.properties`、构建缓存、APK/AAB 和签名材料均未被 Git 跟踪。
- [ ] 在桌面和移动尺寸下检查学习模式、搜索、音频和原页模式。

## 构建 Android 候选包

需要 JDK 17 和 Android SDK Platform 36。设置 `JAVA_HOME`，并设置 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`。首次运行 Gradle Wrapper 需要联网下载 Gradle；已有离线 Gradle 9.4.1 时可设置 `GRADLE_HOME`，然后执行：

```powershell
cd android-app
.\build-apk.ps1
```

生成：

```text
android-app/dist/IELTS-Vocabulary-Offline-Android-1.0.0.apk
android-app/dist/SHA256SUMS.txt
```

脚本默认构建调试签名 APK。面向长期用户发布时，应改用自己的 release 签名配置；签名密钥和密码不得提交到仓库，并应安全备份。更换签名密钥后，Android 通常不能把新包直接作为旧包的更新安装。

## 候选包验证

- [ ] 在至少一台 Android 7.0 或以上设备完成全新安装。
- [ ] 验证 App 可在断网状态启动且不申请网络权限。
- [ ] 验证章节切换、全书搜索、单词音频、例句音频和原页跳转。
- [ ] 验证系统返回键、横竖屏切换及应用进入后台后的音频行为。
- [ ] 使用 `Get-FileHash -Algorithm SHA256 <APK>` 复核校验值。
- [ ] 确认版本号、文件名和 Release Notes 一致。

## 创建 GitHub Release

1. 使用语义化版本标签，例如 `v1.0.0`。
2. 使用 `release/v1.0.0/RELEASE_NOTES.md` 作为发布说明并按实际情况更新。
3. 将 `index.html`、完整 `assets/`、README、版权说明和许可证打包为 `IELTS-Vocabulary-Offline-Web-<版本>.zip`。
4. 将网页版 ZIP、APK 和包含两者校验值的 `SHA256SUMS.txt` 作为 GitHub Release 附件上传，不要把大型发布包提交到 Git 历史。
5. 下载或远端读取已上传的附件并复核文件大小与 SHA-256。
6. 再次确认 Release 页面包含非官方项目声明、第三方内容许可边界和删除请求入口。

## 发现版权问题后的处理

核实权利人请求后，应同时检查并处理：

- 默认分支和其他分支中的相关文件；
- GitHub Release 附件及发布说明；
- README 截图或示例；
- 可能仍包含相关内容的历史标签和派生安装包。

如内容已进入 Git 历史，仅从最新提交删除并不能从历史中彻底移除；必要时应评估历史重写及通知已有克隆者的方案。
