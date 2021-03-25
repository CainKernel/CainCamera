# CainCamera 介绍
CainCamera是一个集美颜相机、图片编辑、短视频编辑等功能的综合性开源APP，目前主要介绍美颜相机、短视频编辑功能的实现，图片编辑功能暂时还没时间处理。
本人编写该项目主要用于学习如何实现相机的实时美颜、动态滤镜、动态贴纸、彩妆、拍照、短视频分段录制与回删、图片编辑、短时频编辑与合成等功能。
截止目前为止，已完成的功能包括：
1、美颜相机部分
* 实时美颜、美白
* 动态滤镜
* 动态贴纸
* 拍照、短视频分段录制、回删等
* 瘦脸大眼、亮眼、美牙等美型处理
## 备注：
由于彩妆功能缺乏各种素材，本人只写了一个大概的流程，彩妆主要是素材绘制有无素材的区别而已，有兴趣的可以参考一下

2、短视频编辑部分
* 仿照抖音编辑功能。目前短视频编辑页面是仿照抖音页面来实现的。
* 自研基于FFmpeg的视频播放器。目前播放器已经包装成MediaPlayer-like的方式，支持快速seek、倍速播放、实时添加特效预览功能。写这个视频播放器的目的也是用于短视频编辑页面的实时预览处理，目前还有不少的BUG，商用的话建议慎重使用
* 简单实现剪辑功能。目前是基于remuxing 来实现的，但还没有对倍速做处理，后续再做处理
* 实时特效。目前已经在播放器上实现了抖音的闪白、幻觉、缩放、抖动、灵魂出窍等滤镜特效以及全部的分屏特效。由于时间特效需要对播放器进行改动，暂时没时间实现。
## 备注：
目前短视频合成功能暂时还没时间实现，目前由于换城市换工作搬家啥的，最近暂时没啥时间，等忙完这段时间，我再把合成功能实现，暂时先别提合成功能相关的issues，谢谢合作。

## 更新
2020年1月1日更新：
目前为了方便接入CameraX库，重写了原来的拍摄页渲染流程以及接入倍速录制功能，目前CameraX库回调数据上面还有些BUG，导致人脸SDK检测出现错乱，这是已知问题，后续有时间再修复。
接下来到春节期间，将会抽时间把编辑页面的合成功能实现，敬请期待。

# 关于人脸SDK验证问题
关于人脸关键点SDK验证问题，由于采用Face++的试用版作为测试的，每天使用的次数有限
所以这里建议大家到Face++官网(https://www.faceplusplus.com/) 注册一个Key使用，需要先注册Key，然后绑定Bundle(包名)才能使用。
国内用户需要到https://www.faceplusplus.com.cn/ 注册。 注册流程如下:
[Face++ SDK注册流程](https://github.com/CainKernel/CainCamera/blob/master/document/introduction/facepp_registration.md)

更多关于Face++ SDK相关的问题，可以到Face++官方github询问：
[MegviiFacepp-Android-SDK](https://github.com/FacePlusPlus/MegviiFacepp-Android-SDK)

# library介绍:
* cameralibrary: 相机库，包括渲染渲染线程、渲染引擎等流程

* facedetectlibrary: Face++人脸关键点SDK库。结合landmarklibrary库做人脸关键点处理。

* filterlibrary：滤镜库。该库存放各个滤镜以及资源处理等工具。

* imagelibrary: 图片编辑库。暂时该库仅有的滤镜处理和保存功能，目前由于正在编写短视频编辑功能的，该库目前暂时没完善。

* landmarklibrary: 关键点处理库。该库用于归一化的关键点处理，用在filterlibrary中处理滤镜、贴纸等处理。

* medialibrary: 短视频编辑库。用于短视频编辑实时预览的播放器、音频裁剪器、视频合成器等全套C++代码。
音视频裁剪器、视频合成器目前仍在开发阶段，敬请期待。

* pickerlibrary: 媒体选择库。用于选择媒体库中的图像、视频。

* utilslibrary: 共用工具库。bitmap处理、文件处理、字符串处理的封装工具。

* videolibrary: 视频编辑库。目前该库处于计划实现状态，由于短视频播放器、短视频合成器等工具还没实现，目前该库暂时还没实现，敬请期待。

## Android Studio 版本问题
2019-01-19 更新：
目前已经更新到Android Studio 3.2.1。
关于Android Studio 3.2.1 之后无法Cmake无法编译通过的问题，这里提一句：
1、Android Studio 3.2.1 更新之后，需要更新NDK、cmake、lldb到最新版本

2、更新之后需要将工程的 dependencies 改成 'com.android.tools.build:gradle:3.2.1'

3、需要将gradle-wrapper.properies的版本变成 4.6，即 gradle-4.6-all.zip

原因：NDK跟SDK的Gradle版本不同步导致的。
默认升级后会自动更新成 'com.android.tools.build:gradle:3.3.0' 和 gradle-4.10.1-all.zip
如果使用上述配置，用Cmake编译NDK文件，会出现诸如 Error Config、Cmake 3.6.0 跟SDK的Cmake3.10.2.xxx冲突之类的出错提示
目前已经经过Windows、Linux和MacOS 均已通过验证，编译不过的同学，请检查自身的配置。

# CainCamera截图
## 动态贴纸与动态滤镜功能
![贴纸和滤镜](https://github.com/CainKernel/CainCamera/blob/master/screenshot/sticker_and_filter.jpg)

![动态滤镜](https://github.com/CainKernel/CainCamera/blob/master/screenshot/dynamic_filter.jpg)

## 人脸美化与美型处理
![人脸美化](https://github.com/CainKernel/CainCamera/blob/master/screenshot/beauty_face.jpg)

![美型处理](https://github.com/CainKernel/CainCamera/blob/master/screenshot/face_reshape.jpg)

## 彩妆功能
* 备注：由于缺乏素材，这里只展示彩妆功能是如何通过遮罩来实现。

![动态彩妆](https://github.com/CainKernel/CainCamera/blob/master/screenshot/makeup.jpg)

## 媒体库遍历
![媒体库遍历](https://github.com/CainKernel/CainCamera/blob/master/screenshot/media_scan.jpg)

## 图片编辑页面
* 备注：图片编辑功能暂时没有时间实现所有的功能

![图片编辑页面](https://github.com/CainKernel/CainCamera/blob/master/screenshot/image_edit.jpg)

# CainCamera 参考项目：
[grafika](https://github.com/google/grafika)

[GPUImage](https://github.com/CyberAgent/android-gpuimage)

[MagicCamera](https://github.com/wuhaoyu1990/MagicCamera)

[AudioVideoRecordingSample](https://github.com/saki4510t/AudioVideoRecordingSample)

# 《Android 美颜类相机开发汇总》
[第一章 Android OpenGLES 相机预览](https://www.jianshu.com/p/dabc6be45d2e)

[第二章 Android OpenGLES 录制视频](https://www.jianshu.com/p/d5fe577170cd)

[第三章 Android OpenGLES 给相机添加滤镜](https://www.jianshu.com/p/f7629254f7f0)

[第四章 Android OpenGLES 动态贴纸实现](https://www.jianshu.com/p/122bedf3a17e)

[第五章 Android OpenGLES 美颜定制实现](https://www.jianshu.com/p/3334a3af331f)

[第六章 Android OpenGLES 美妆定制实现](https://www.jianshu.com/p/bc0d0db2893b)

# 《Android FFmpeg 播放器开发梳理》
[第零章 基础公共类的封装](https://www.jianshu.com/p/9003caa6683f)

[第一章 播放器初始化与解复用流程](https://www.jianshu.com/p/95dc19217847)

[第二章 音视解码器和视频解码器实现](https://www.jianshu.com/p/8de0fc796ef9)

[第三章 音频输出 —— OpenSLES](https://www.jianshu.com/p/9b41212c71a5)

[第四章 音频重采样与变速变调处理](https://www.jianshu.com/p/4af5d16ac017)

[第五章 视频同步渲染输出](https://www.jianshu.com/p/f8ba3ceac687)

# 求职意向
在腾讯微视待了两年，经历了微视SDK团队的拆分，一直没给机会做音视频、OpenGL相关开发。目前已在计划离职阶段，在看广深的机会，不知是否有做音视频、OpenGL开发的坑？最近面了将近一个月，都没找到比较合适的岗位。如有合适的岗位，欢迎推荐

# 个人联系方式

email: <cain.huang@outlook.com>

blog: [cain_huang](http://www.jianshu.com/u/fd6f2b25d0f4)

# License
```
Copyright 2018 cain.huang@outlook.com
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
