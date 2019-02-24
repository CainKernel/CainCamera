# CainCamera 介绍
CainCamera是一个集美颜相机、图片编辑、短视频编辑等功能的综合性开源APP。
本人编写该项目主要用于学习如何实现相机的实时美颜、动态滤镜、动态贴纸、彩妆、拍照、短视频分段录制与回删、图片编辑、短时频编辑与合成等功能。
截止目前为止，已完成的功能包括：
* 实时美颜、美白
* 动态滤镜
* 动态贴纸
* 拍照、短视频分段录制、回删等
* 瘦脸大眼、亮眼、美牙等美型处理
备注：由于彩妆功能缺乏各种素材，本人只写了一个大概的流程，彩妆主要是素材绘制有无素材的区别而已，有兴趣的可以参考一下。

注意事项：关于人脸关键点SDK验证问题，由于采用Face++的试用版作为测试的，每天使用的次数有限
所以这里建议大家到Face++官网(https://www.faceplusplus.com/)注册一个Key使用
要不然我这边每次想要更新功能都要重新搞一个key，比较麻烦。谢谢大家合作。

# library介绍:
* cameralibrary: 相机库，包括渲染渲染线程、渲染引擎等流程

* facedetectlibrary: Face++人脸关键点SDK库。结合landmarklibrary库做人脸关键点处理。

* filterlibrary：滤镜库。该库存放各个滤镜以及资源处理等工具。

* imagelibrary: 图片编辑库。暂时该库仅有的滤镜处理和保存功能，目前由于正在编写短视频编辑功能的，该库目前暂时没完善。

* landmarklibrary: 关键点处理库。该库用于归一化的关键点处理，用在filterlibrary中处理滤镜、贴纸等处理。

* medialibrary: 短视频编辑库。用于短视频编辑实时预览的播放器、音频裁剪器、视频合成器等全套代码，其中：
播放器已实现软硬解码(FFmpeg + MediaCodec)、音视频多种同步方式、定位播放、
倍速播放(硬解码时支持1080P的4倍速播放而音频无杂音等问题)、支持添加实时特效等功能，逆序播放功能暂未实现。
音视频裁剪器、视频合成器目前仍在开发阶段，敬请期待。

* scanlibrary: 媒体扫描库。用于遍历扫描媒体库中的图像、视频。

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
