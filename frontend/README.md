# 录音录像系统 - 前端

基于 Vue 3 + Element Plus 的免驱录音录像系统 Web 前端。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Element Plus** - Vue 3 UI 组件库
- **Pinia** - 状态管理
- **Vue Router** - 路由管理
- **Axios** - HTTP 请求
- **Vite** - 构建工具
- **Sass** - CSS 预处理器

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器（默认端口 3000）
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## 项目结构

```
frontend/
├── index.html                  # HTML 入口
├── package.json                # 依赖配置
├── vite.config.js              # Vite 配置
└── src/
    ├── main.js                 # 应用入口
    ├── App.vue                 # 根组件（布局）
    ├── router/index.js         # 路由配置
    ├── api/index.js            # 后端 API 封装
    ├── stores/                 # Pinia 状态管理
    │   ├── device.js           # 设备状态
    │   ├── recording.js        # 录像状态
    │   └── files.js            # 文件状态
    ├── utils/                  # 工具函数
    │   ├── request.js          # Axios 实例
    │   └── helpers.js          # 格式化工具
    ├── views/                  # 页面视图
    │   ├── RecordingView.vue   # 录音录像（主页）
    │   ├── DevicesView.vue     # 设备管理
    │   ├── FilesView.vue       # 文件管理
    │   └── SettingsView.vue     # 系统设置
    └── assets/styles/
        └── global.scss         # 全局样式
```

## 页面说明

### 录音录像（主页）
- 实时视频预览（MJPEG 流）
- 设备快速切换（摄像头/麦克风选择）
- 录制控制（开始/暂停/停止）
- 拍照功能
- 录制模式切换（音视频/仅视频/仅音频）
- 实时状态显示（时长/文件大小/分辨率）
- 最近录像列表

### 设备管理
- 扫描和展示所有 UVC/UAC 设备
- 设备选择和状态查看
- 设备格式和参数显示

### 文件管理
- 录像文件列表（支持搜索和筛选）
- 文件播放、下载、重命名、删除
- 文件类型筛选（视频/音频/图片）

### 系统设置
- 视频参数（分辨率/帧率/编码/比特率）
- 音频参数（采样率/位深度/声道/编码）
- 存储设置（路径/格式/自动分割/最长录制）

## 后端接口

前端通过 `/api` 前缀调用后端接口，开发时通过 Vite 代理转发到 `http://localhost:8080`。

### 主要接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/devices/cameras` | GET | 获取摄像头列表 |
| `/api/devices/audio` | GET | 获取音频设备列表 |
| `/api/recording/start` | POST | 开始录制 |
| `/api/recording/stop` | POST | 停止录制 |
| `/api/recording/pause` | POST | 暂停录制 |
| `/api/recording/photo` | POST | 拍照 |
| `/api/preview/stream` | GET | 获取预览流（MJPEG） |
| `/api/files` | GET | 获取文件列表 |
| `/api/files/:id/download` | GET | 下载文件 |
| `/api/settings` | GET/PUT | 获取/保存设置 |

## 浏览器兼容性

- Chrome 90+
- Firefox 90+
- Edge 90+
- Safari 15+

## 许可证

MIT License
