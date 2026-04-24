# GitHub Uploader - Android

一个用于简化GitHub仓库文件上传的Android应用，使用Kotlin和Jetpack Compose构建。

## 功能特性

- **Token认证**：使用GitHub个人访问令牌进行安全认证
- **仓库管理**：
  - 使用现有仓库
  - 创建新仓库
- **文件上传**：
  - 上传YAML工作流文件
  - 打包并上传整个文件夹
  - 上传ZIP文件
  - 预置unpack.yml和build.yml工作流模板
- **仓库内容浏览**：查看仓库文件和文件夹
- **剪贴板片段管理**：保存和管理常用文本片段
- **灵活配置**：
  - 自定义构建配置
  - 打包排除规则
  - 显示缩放设置

## 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose + Material3
- **架构**：MVVM
- **网络**：Retrofit2 + OkHttp
- **JSON**：Moshi
- **异步**：Kotlin Coroutines

## 项目结构

```
com.example.githubuploader/
├── data/
│   ├── local/          # 本地存储
│   ├── model/         # 数据模型
│   └── remote/       # 网络层
├── domain/          # 业务逻辑
├── ui/              # UI层
│   ├── navigation/  # 导航
│   └── screens/    # 屏幕
└── util/            # 工具类
```

## 开始使用

### 构建项目要求

- Android Studio Hedgehog | 2023.1.1 或更高版本
- Android SDK API 24 (Android 7.0) 或更高版本
- Gradle 8.2
- JDK 17

### 构建步骤

1. 克隆项目
2. 在Android Studio中打开项目
3. 等待Gradle同步完成
4. 运行应用

### GitHub Token

1. 前往 GitHub 设置 → Developer settings → Personal access tokens
2. 生成新token，选择repo权限
3. 在应用中输入token进行认证

## 主要功能说明

### 上传屏幕

1. 认证：输入GitHub个人访问令牌
2. 仓库设置：选择现有仓库或创建新仓库
3. 文件选择：添加YAML文件、选择文件夹或ZIP文件
4. 上传：执行上传操作

### 仓库内容屏幕

1. 输入仓库URL
2. 查看仓库文件列表
3. 刷新内容

### 片段屏幕

1. 选择已保存的片段
2. 复制到剪贴板
3. 编辑和管理片段

### 设置屏幕

1. 显示设置：字体和对话框缩放
2. 基本设置：Token、默认分支等
3. 构建配置：Java版本、构建类型等
4. 排除规则：打包时的文件排除模式

## 许可证

MIT License
