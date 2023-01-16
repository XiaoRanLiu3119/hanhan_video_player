# hanhan_video_player
一款使用kotlin开发的本地视频播放器.
经常上下班在没网的地铁上看下好的电影,暂未发现适合自己的的播放器,写来自用,提供给需要的   
基于[ GSYVideoPlayer](https://github.com/CarGuo/GSYVideoPlayer)
截图   
![主页面](https://github.com/XiaoRanLiu3119/hanhan_video_player/blob/master/screenshot/main.jpg)
![视频列表](https://github.com/XiaoRanLiu3119/hanhan_video_player/blob/master/screenshot/video_list.jpg)
![播放器](https://github.com/XiaoRanLiu3119/hanhan_video_player/blob/master/screenshot/player.jpg)
![设置](https://github.com/XiaoRanLiu3119/hanhan_video_player/blob/master/screenshot/setting.jpg)
## 提示
项目代码为熟悉kotlin所编写,且未做优化,仅供参考
## 开发环境
- Android Studio Chipmunk 2021.2.1
- kotlin.android 1.7.10
- gradle:7.3.3-bin
- gradle plugin version:7.2.2
## 项目中用到的库,感谢大佬们
- RecyclerView框架 [ BRV](https://github.com/liangjingkanji/BRV)
- 权限 [ XXPermissions](https://github.com/getActivity/XXPermissions)
- autosize
- immersionbar
- XPopup
- ShadowLayout
- TitleBar
- 更多详见app下的build.gradle
## 进度
- [x] 播放历史
- [x] 长按倍速播放
- [x] 切换播放器内核(ijk/exo/系统)
- [x] 清除缓存
- [ ] 长按开启倍速反馈(震动和倍速图标显示)
- [ ] 多选操作
- [ ] 优化ui(弹窗等)
- [ ] 多选删除等
- [ ] GSY提供的更多设置项
