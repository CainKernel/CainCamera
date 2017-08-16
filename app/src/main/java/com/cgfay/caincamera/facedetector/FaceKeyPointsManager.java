package com.cgfay.caincamera.facedetector;

/**
 * 人脸关键点管理器
 * Created by cain on 2017/8/16.
 */
public class FaceKeyPointsManager {

    // ------- 106个关键点 -------//
    enum Points106 {
        ;
        /************* 脸颊及下巴 **************/
        // 0 ~ 32 是脸的边缘关键点
        int chin_center = 16; // 下巴正中心点
        int chin_left = 12; // 下巴左边起始点
        int chin_right = 20; // 下巴右边结束点

        /************ 眉毛 ***********/
        // 33 ~ 37 是左眉毛上边关键点
        int eyebrow_left_start = 33; // 左眉毛起始点
        int eyebroe_left_up_center = 35; // 左眉毛上边间点
        int eyebrow_left_up_end = 37; // 左眉毛上边结束点
        // 64 ~ 67 是左眉毛下边关键点
        int eyebrow_left_down_center = 65; // 左眉毛下边中间点
        int eyebrow_left_down_end = 67; // 左眉毛下边结束点

        // 38 ~ 42 是右眉毛上边的关键点(从左往右)
        int eyebrow_right_up_start = 37; // 右眉毛上边起始点
        int eyebrow_right_up_center = 40; // 右眉毛上边中心点
        int eyebrow_right_end = 42; // 右眉毛结束点
        // 68 ～ 71 是右眉毛下边的关键点(从左往右)
        int eyebrow_right_down_start = 68; // 右眉毛下边起始点
        int eyebrow_right_down_center = 70; // 右眉毛下边中心点

        /************ 眼睛 ************/
        // 52 ～ 57 是左眼边框关键点(顺时针)
        int eye_left_horizontal_start = 55; // 左边左边起始点
        int eye_left_horizontal_end = 55; // 左眼右边结束点
        // 72 ~ 74 是左眼中心竖直线上的点
        int eye_left_vertical_start = 72; // 左眼竖直方向起始点
        int eye_left_vertical_end = 73; // 左眼竖直方向结束点
        int eye_left_center = 74; // 左眼中心点

        // 58 ~ 63 是右眼边框关键点(顺时针)
        int eye_right_horizontal_start = 58; // 右眼左边起始点
        int eye_right_horizontal_end = 61; // 右眼右边结束点
        // 75 ~ 77 是右眼中心竖直线上的点
        int eye_right_vertical_start = 75; // 右眼竖直方向起始点
        int eye_right_vertical_end = 76; // 右眼竖直方向结束点
        int eye_right_center = 77; // 右眼中心点

        /**************鼻子和鼻孔、鼻子边缘*****************/
        // 44 ~ 49 是鼻子竖直方向的点
        int nose_vertical_start = 44; // 鼻子竖直方向起始点
        int nose_vertical_end = 49; // 鼻子竖直方向结束点(鼻尖)
        // 47 ～51 是左右两个鼻孔的点
        // 80 ~ 83 是鼻子隆起部分的边缘

        /************** 嘴巴和嘴唇 ***************/
        // 96 ～ 103是嘴唇内边框(顺时针)
        int lips_inner_start = 96; // 嘴唇内边缘左边起始点
        int lips_inner_end = 100; // 嘴唇内边缘右边结束点
        int lips_inner_up_center = 98; // 嘴唇内边缘上边的中心点
        int lips_inner_down_center = 102; // 嘴唇哪边缘下边的中心点
        // 84 ～ 95是嘴唇外边款(顺时针)
        int lips_outer_start = 84; // 嘴唇外边缘左边起始点
        int lips_outer_end = 90; // 嘴唇外边缘右边结束点
        int lips_outer_up_center = 87; // 嘴唇外边缘上边的中心点
        int lips_outer_down_center = 93; // 嘴唇外边缘下边的中心点

        /************** 印堂穴 ************/
        // 43、78、79 三个点是印堂穴附近的点
    }

    // ------- 81个关键点 -------//
    enum Points81 {
        ;
        /************* 脸颊及下巴 **************/
        // 62是脸颊左边起始点(眼角连线上)， 63是脸颊右边结束点(眼角连线上), 64是下巴中心点
        // 65 ~ 72 是左边脸颊(逆时针顺序)，73 ～ 80 是右边脸颊(顺时针)
        int chin_center = 64; // 下巴正中心点
        int chin_left = 70; // 下巴左边起始点
        int chin_right = 78; // 下巴右边结束点

        /************ 眉毛 ***********/
        // 22、20、24 是左眉毛上边关键点
        //    22  20  24
        // 18            19
        //    23  21  25
        int eyebrow_left_start = 18; // 左眉毛起始点
        int eyebrow_left_end = 19; // 左眉毛右边结束点
        int eyebroe_left_up_center = 20; // 左眉毛上边间点
        int eyebrow_left_up_end = 24; // 左眉毛上边结束点
        // 23、21、25 是左眉毛下边关键点
        int eyebrow_left_down_center = 21; // 左眉毛下边中间点
        int eyebrow_left_down_end = 25; // 左眉毛下边结束点

        // 30、28、32 是右眉毛上边的关键点(从左往右)
        //    30  28  32
        // 26            27
        //    31  29  33
        int eyebrow_right_start = 26; // 右眉毛左边起始点
        int eyebrow_right_end = 27; // 右眉毛结束点
        int eyebrow_right_up_start = 30; // 右眉毛上边起始点
        int eyebrow_right_up_center = 28; // 右眉毛上边中心点
        // 31、29、33 是右眉毛下边的关键点(从左往右)
        int eyebrow_right_down_start = 29; // 右眉毛下边起始点
        int eyebrow_right_down_center = 31; // 右眉毛下边中心点

        /************ 眼睛 ************/
        // 0 ～ 8 是左眼边框关键点
        //    5  3  7
        // 1     0     2
        //    6  4  8
        int eye_left_horizontal_start = 1; // 左边左边起始点
        int eye_left_horizontal_end = 2; // 左眼右边结束点
        int eye_left_vertical_start = 3; // 左眼竖直方向起始点
        int eye_left_vertical_end = 4; // 左眼竖直方向结束点
        int eye_left_center = 0; // 左眼中心点

        // 9 ~ 17 是右眼边框关键点
        //    14 12 16
        // 10    9    11
        //    15 13 17
        int eye_right_horizontal_start = 10; // 右眼左边起始点
        int eye_right_horizontal_end = 11; // 右眼右边结束点
        int eye_right_vertical_start = 12; // 右眼竖直方向起始点
        int eye_right_vertical_end = 13; // 右眼竖直方向结束点
        int eye_right_center = 9; // 右眼中心点

        /**************鼻子和鼻孔、鼻子边缘*****************/
        // 34、35 是鼻子竖直方向的点，38 ～43 是鼻子边缘
        //     38        39
        //          34
        //  40              41
        //      42      43
        //          35
        int nose_vertical_start = 34; // 鼻子竖直方向起始点
        int nose_vertical_end = 35; // 鼻子竖直方向结束点(鼻尖)

        /************** 嘴巴和嘴唇 ***************/
        // 44 ~ 61 是嘴唇边框
        //             48     49
        //         50     46      51
        //           52   47   53
        //    44                       45
        //           56        57
        //         58     54      61
        //             59     60
        //                55
        int lips_inner_start = 44; // 嘴唇内边缘左边起始点
        int lips_inner_end = 45; // 嘴唇内边缘右边结束点
        int lips_inner_up_center = 47; // 嘴唇内边缘上边的中心点
        int lips_inner_down_center = 54; // 嘴唇内边缘下边的中心点

        int lips_outer_start = 44; // 嘴唇外边缘左边起始点
        int lips_outer_end = 45; // 嘴唇外边缘右边结束点
        int lips_outer_up_center = 46; // 嘴唇外边缘上边的中心点
        int lips_outer_down_center = 55; // 嘴唇外边缘下边的中心点

        /************** 印堂穴 ************/
        // 36、37两个点是印堂穴附近的点
    }
}
