# 雅思词汇真经 Android 离线版

这是原离线网页的 Android 原生容器工程。应用不申请网络权限，使用 AndroidX
`WebViewAssetLoader` 从 APK 内部安全加载全部页面、卡片和音频。

> 本项目为个人学习过程中开发的非商业开源项目，与相关作者、出版社、教育机构及品牌方
> 不存在官方合作、授权、赞助或隶属关系。开源许可证仅适用于原创软件代码及开发者有权
> 许可的内容，不适用于教材、音频、图片等第三方作品。“不收费”或“非商业使用”并不
> 当然意味着第三方内容可以未经授权自由复制或传播。完整说明见上级目录的 `COPYRIGHT.md`。

## 功能

- 保留 22 章、3,670 个词条、全书搜索、词条卡片、单词/例句音频。
- 保留 337 页原页阅读、页码输入和词条到原页的跳转。
- 自适应手机、平板、横屏、刘海屏和 Android 系统栏。
- 记住上次阅读章节、页码和学习/原页模式。
- Android 返回键依次停止音频、关闭目录、清除搜索、退出原页模式、退出应用。
- 应用进入后台时自动停止当前音频。
- 不申请 `INTERNET` 权限，不会上传学习数据。

## 工程与资源

Android 工程位于 `android-app/`，原始网页和 `assets/` 仍保留在上级目录。
构建任务会生成 Android 专用 `index.html` 并直接复用上级目录的媒体资源，因此源码
目录不会再复制一份约 675 MB 的素材。

不要单独移动 `android-app/`；它需要与上级的 `index.html` 和 `assets/` 保持当前关系。

## 构建

要求 JDK 17 和 Android SDK Platform 36。设置 `JAVA_HOME`，并设置 `ANDROID_HOME`
或 `ANDROID_SDK_ROOT`。工程默认使用仓库自带的 Gradle Wrapper；首次运行 Wrapper 需要
下载 Gradle。已有离线 Gradle 9.4.1 时，可将其目录设置为 `GRADLE_HOME`。

在 PowerShell 中运行：

```powershell
.\build-apk.ps1
```

生成的可安装 APK 位于：

```text
dist/IELTS-Vocabulary-Offline-Android-1.0.0.apk
dist/SHA256SUMS.txt
```

当前脚本生成的是 Android 调试签名 APK，适合本地安装和测试。准备上架应用商店前，
应配置你自己的长期 release 签名密钥，并根据商店要求考虑 Play Asset Delivery。

`dist/`、构建目录、本机 SDK 配置和签名材料均不应提交到 Git。发布流程和检查项见
上级目录的 `docs/RELEASING.md`。
