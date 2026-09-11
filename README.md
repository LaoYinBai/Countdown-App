# 🗓️ 倒数日 - CountdownApp

一款简洁优雅的 Android 倒数日应用，支持**阳历 / 农历**日期追踪、**纪念日倒计时**、**背景自定义**等功能。用 Jetpack Compose 构建，纯 Kotlin 实现。

---

## ✨ 功能一览

### 📌 事件管理

- **添加事件** — 输入名称，选择目标日期（阳历或农历），一键添加
- **置顶功能** — 重要事件置顶显示（最多 5 个），首页优先展示，采用大卡片样式
- **滑动操作** — 左滑快速删除，右滑快速置顶/取消置顶，交互流畅
- **批量管理** — 长按进入编辑模式，支持多选、全选、批量删除
- **事件详情** — 点击卡片进入大卡片详情页，展示大字倒计时、起始日期

### 📅 农历支持

- 阳历/农历双模式切换，农历日期使用 `cn.6tail:lunar` 库精确转换
- 农历生日模式：勾选「每年重复」，每年按农历日期自动计算倒计时
- 农历月份以中文显示（正月、腊月等），日期以中文显示（初一、十五等）

### 🖼️ 个性化卡片

- **自定义背景图** — 从相册选取图片作为事件卡片背景
- **自动调色** — 根据背景图亮度自动切换黑白文字，保证可读性
- **保存为图片** — 详情页右上角一键将卡片保存为 PNG 到手机相册
- **动态颜色** — 事件名称通过哈希算法自动生成专属主题色（左侧色条）

### 🎨 交互体验

- **毛玻璃动效** — 详情页弹出时背景模糊渐变，聚焦当前事件
- **滑动互斥** — 多个滑动卡片互不干扰，滚动列表自动关闭展开的滑动
- **空状态提示** — 无事件时显示友好提示文字

---

## 📱 截图预览

| 首页列表 | 事件详情 | 添加事件 |
|:---:|:---:|:---:|
| 事件卡片列表，置顶大卡片优先展示 | 大字倒计时 + 背景 + 操作按钮 | 阳历/农历切换，滚轮选日期 |

---

## 🏗️ 技术架构

```
CountdownApp/
├── app/
│   ├── src/main/java/com/countdownapp/
│   │   ├── CountdownApp.kt              # Application 入口
│   │   ├── MainActivity.kt              # 主 Activity + 全局 UI 组合
│   │   ├── data/
│   │   │   ├── entity/Event.kt          # Room 实体（事件模型）
│   │   │   ├── db/AppDatabase.kt        # Room 数据库定义
│   │   │   ├── db/EventDao.kt           # 数据访问对象
│   │   │   └── repository/EventRepository.kt  # 仓库层
│   │   ├── ui/
│   │   │   ├── theme/                   # Material3 主题 & 颜色
│   │   │   ├── components/              # 可复用 UI 组件
│   │   │   │   ├── EventCard.kt         # 事件卡片（普通/置顶大卡片）
│   │   │   │   ├── AddEventDialog.kt    # 添加事件弹窗
│   │   │   │   └── SwipeToDelete.kt     # 左右滑动组件
│   │   │   ├── screens/
│   │   │   │   ├── MainScreen.kt        # 首页列表
│   │   │   │   └── EventDetailScreen.kt # 详情大卡片页
│   │   │   └── viewmodel/
│   │   │       └── MainViewModel.kt     # 主 ViewModel
│   │   └── util/
│   │       ├── DateUtils.kt             # 日期计算 & 颜色生成
│   │       └── LunarDateUtils.kt        # 农历转换工具
│   ├── src/main/res/                    # 资源文件（字符串、颜色、图标）
│   └── build.gradle.kts                 # 模块构建配置
├── build.gradle.kts                     # 根项目构建配置
├── settings.gradle.kts                  # Gradle 设置（含阿里云镜像）
├── gradle.properties                    # Gradle 属性
├── gradlew.bat                          # Gradle Wrapper
└── .gitignore                           # Git 忽略规则
```

### 核心技术

| 层 | 技术 | 用途 |
|:---|:---|:---|
| **UI** | Jetpack Compose + Material 3 | 声明式 UI 构建 |
| **导航** | Navigation Compose | 页面路由（单 Activity 架构） |
| **数据库** | Room (KSP) | 本地持久化事件数据 |
| **图片加载** | Coil | 异步加载本地背景图片 |
| **农历** | cn.6tail:lunar | 阳历/农历双向转换 |
| **状态管理** | ViewModel + StateFlow | 响应式 MVVM 架构 |
| **动画** | Compose Animation | 卡片展开、毛玻璃渐变动效 |

---

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Gradle 8.5
- Android SDK 34
- Min SDK: 26 (Android 8.0)

### 构建运行

```bash
# 克隆项目
git clone https://github.com/yourusername/CountdownApp.git

# 用 Android Studio 打开项目
cd CountdownApp

# 或者命令行构建
./gradlew assembleDebug
```

> 国内用户注意：`settings.gradle.kts` 已配置阿里云 Maven 镜像，加速依赖下载。

---

## 📖 使用指南

详见 [USER_GUIDE.md](USER_GUIDE.md)，涵盖：

1. **添加新事件** — 阳历/农历选择、日期滚轮
2. **滑动删除** — 左滑删除 + 置顶取消置顶
3. **编辑模式** — 长按进入批量选择/删除
4. **详情页操作** — 修改名称、修改日期、设置背景
5. **保存卡片图片** — 一键导出为图片
6. **置顶事件** — 置顶大卡片样式

---

## 📦 构建产物

- **APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **AAB**: `app/build/outputs/bundle/debug/app-debug.aab`（上架 Google Play 用）

---

## 🧪 开发进度

详见 [PROGRESS.md](PROGRESS.md)：

- ✅ 核心滑动交互（左滑删除、右滑置顶、展开互斥）
- ✅ 多选编辑模式（复选框 + 全选/全不选 + 批量删除）
- ✅ 农历基础框架（农历/阳历 Tab 切换）
- ✅ 置顶大卡片样式（2 倍高度、更多信息展示）
- ✅ 详情页导出图片至相册
- ⏳ 农历日期在卡片上的完整格式化显示

---

## 🤝 贡献

欢迎提交 Issue 或 PR！主要待改进方向：

- 农历日期在首页卡片上的完整显示
- 深色模式支持
- 事件分类/标签
- 小组件 (Widget)

---

## 📄 许可证

```
MIT License

Copyright (c) 2026 CountdownApp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```

---

*CountdownApp — 不错过每一个重要的日子* 🎯