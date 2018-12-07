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
* ffmpeglibrary: 基于FFmpeg开发的工具库，目前实现了音乐播放器、MetadataRetriever等工具，流媒体播放器、短视频播放器、短视频合成器等工具处于开发阶段，敬请期待。
* filterlibrary：滤镜库。该库存放各个滤镜以及资源处理等工具。
* imagelibrary: 图片编辑库。暂时该库仅有的滤镜处理和保存功能，目前由于正在编写短视频编辑功能的，该库目前暂时没完善。
* landmarklibrary: 关键点处理库。该库用于归一化的关键点处理，用在filterlibrary中处理滤镜、贴纸等处理。
* medialibrary: 媒体扫描库。用于扫描媒体库中的图像、视频。
* utilslibrary: 共用工具库。bitmap处理、文件处理、字符串处理的封装工具。
* videolibrary: 视频编辑库。目前该库处于计划实现状态，由于短视频播放器、短视频合成器等工具还没实现，目前该库暂时还没实现，敬请期待。

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

# 个人联系方式

email: <cain.huang@outlook.com>

blog: [cain_huang](http://www.jianshu.com/u/fd6f2b25d0f4)