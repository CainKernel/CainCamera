package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.cgfay.caincamera.R;

/**
 * 裁剪旋转编辑器
 * Created by Administrator on 2018/3/12.
 */

public class CropRotateEditor extends BaseEditor
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private RelativeLayout mLayoutCropRotate;

    private SeekBar mSeekBar;

    private Button mBtnRotate90;
    private Button mBtnFlip;
    private Button mBtnCustom;
    private Button mBtnOriginal;
    private Button mBtnOneToOne;
    private Button mBtnThreeToFour;
    private Button mBtnFourToThree;
    private Button mBtnNineToSixteen;
    private Button mBtnSixteenToNine;

    public CropRotateEditor(Context context, ImageEditManager manager) {
        super(context, manager);
    }


    @Override
    protected void initView() {
        mLayoutCropRotate = (RelativeLayout) mInflater
                .inflate(R.layout.view_image_edit_croprotate, null);
        mBtnRotate90 = (Button) mLayoutCropRotate.findViewById(R.id.btn_rotate_90);
        mBtnFlip = (Button) mLayoutCropRotate.findViewById(R.id.btn_flip);
        mBtnCustom = (Button) mLayoutCropRotate.findViewById(R.id.btn_custom);
        mBtnOriginal = (Button) mLayoutCropRotate.findViewById(R.id.btn_original);
        mBtnOneToOne = (Button) mLayoutCropRotate.findViewById(R.id.btn_one_to_one);
        mBtnThreeToFour = (Button) mLayoutCropRotate.findViewById(R.id.btn_three_to_four);
        mBtnFourToThree = (Button) mLayoutCropRotate.findViewById(R.id.btn_four_to_three);
        mBtnNineToSixteen = (Button) mLayoutCropRotate.findViewById(R.id.btn_nine_to_sixteen);
        mBtnSixteenToNine = (Button) mLayoutCropRotate.findViewById(R.id.btn_sixteen_to_nine);

        mBtnRotate90.setOnClickListener(this);
        mBtnFlip.setOnClickListener(this);
        mBtnCustom.setOnClickListener(this);
        mBtnOriginal.setOnClickListener(this);
        mBtnOneToOne.setOnClickListener(this);
        mBtnThreeToFour.setOnClickListener(this);
        mBtnFourToThree.setOnClickListener(this);
        mBtnNineToSixteen.setOnClickListener(this);
        mBtnSixteenToNine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rotate_90:
                rotateBitmap(90);
                break;

            case R.id.btn_flip:
                flipBitmap();
                break;

            case R.id.btn_custom:
                break;

            case R.id.btn_original:
                break;

            case R.id.btn_one_to_one:
                break;

            case R.id.btn_three_to_four:
                break;

            case R.id.btn_four_to_three:
                break;

            case R.id.btn_nine_to_sixteen:
                break;

            case R.id.btn_sixteen_to_nine:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public RelativeLayout getLayoutCropRotate() {
        return mLayoutCropRotate;
    }

    @Override
    public void updateBitmapTexture() {
        if (mWeakManager != null && mWeakManager.get() != null) {
            mWeakManager.get().updateBitmapTexture();
        }
    }

    /**
     * 旋转图片
     * @param rotate
     */
    private void rotateBitmap(int rotate) {
        if (mWeakManager != null && mWeakManager.get() != null) {
            mWeakManager.get().rotateBitmap(rotate);
        }
    }

    /**
     * 镜像翻转
     */
    private void flipBitmap() {
        if (mWeakManager != null && mWeakManager.get() != null) {
            mWeakManager.get().flipBitmap();
        }
    }

    /**
     * 裁剪图片
     * @param x
     * @param y
     * @param width
     * @param height
     */
    private void cropBitmap(int x, int y, int width, int height) {

    }

}
