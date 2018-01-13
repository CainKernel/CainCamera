package com.cgfay.cainfilter.utils.facepp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.PowerManager;
import android.util.Base64;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ConUtil {
	
	public static boolean isReadKey(Context context) {
		InputStream inputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int count = -1;
		try {
			inputStream = context.getAssets().open("key");
			while ((count = inputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, count);
			}
			byteArrayOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String str = new String(byteArrayOutputStream.toByteArray());
		String key = null;
		String screct = null;
		try {
			String[] strs = str.split(";");
			key = strs[0].trim();
			screct = strs[1].trim();
		} catch (Exception e) {
		}
		Util.API_KEY = key;
		Util.API_SECRET = screct;
		if (Util.API_KEY == null || Util.API_SECRET == null)
			return false;

		return true;
	}
	
	/**
	 * 时间格式化(格式到秒)
	 */
	public static String getFormatterDate(long time) {
		Date d = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String data = formatter.format(d);
		return data;
	}
	
	public static String getUUIDString(Context mContext) {
		String KEY_UUID = "key_uuid";
		SharedUtil sharedUtil = new SharedUtil(mContext);
		String uuid = sharedUtil.getStringValueByKey(KEY_UUID);
		if (uuid != null && uuid.trim().length() != 0)
			return uuid;

		uuid = UUID.randomUUID().toString();
		uuid = Base64.encodeToString(uuid.getBytes(),
				Base64.DEFAULT);

		sharedUtil.saveStringValue(KEY_UUID, uuid);
		return uuid;
	}

	public static Bitmap decodeToBitMap(byte[] data, Camera _camera) {
		Camera.Size size = _camera.getParameters().getPreviewSize();
		try {
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
			if (image != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
				Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
				stream.close();
				return bmp;
			}
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * 隐藏软键盘
	 */
	public static void isGoneKeyBoard(Activity activity) {
		if (activity.getCurrentFocus() != null) {
			// 隐藏软键盘
			((InputMethodManager) activity
					.getSystemService(activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(activity.getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	public static PowerManager.WakeLock wakeLock = null; 

	public static void acquireWakeLock(Context context) {
		if (wakeLock == null) {
			PowerManager powerManager = (PowerManager) (context
					.getSystemService(Context.POWER_SERVICE));
			wakeLock = powerManager.newWakeLock(
					PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
			wakeLock.acquire();
		}
	}

	public static void releaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	/**
	 * 获取bitmap的灰度图像
	 */
	public static byte[] getGrayscale(Bitmap bitmap) {
		if (bitmap == null)
			return null;

		byte[] ret = new byte[bitmap.getWidth() * bitmap.getHeight()];
		for (int j = 0; j < bitmap.getHeight(); ++j)
			for (int i = 0; i < bitmap.getWidth(); ++i) {
				int pixel = bitmap.getPixel(i, j);
				int red = ((pixel & 0x00FF0000) >> 16);
				int green = ((pixel & 0x0000FF00) >> 8);
				int blue = pixel & 0x000000FF;
				ret[j * bitmap.getWidth() + i] = (byte) ((299 * red + 587
						* green + 114 * blue) / 1000);
			}
		return ret;
	}

	public static byte[] getFileContent(Context context, int id) {
		InputStream inputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int count = -1;
		try {
			inputStream = context.getResources().openRawResource(id);
			while ((count = inputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, count);
			}
			byteArrayOutputStream.close();
		} catch (IOException e) {
			return null;
		} finally {
			// closeStreamSilently(inputStream);
			inputStream = null;
		}
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * 输出toast
	 */
	public static void showToast(Context context, String str) {
		if (context != null) {
			Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
			// 可以控制toast显示的位置
			toast.setGravity(Gravity.TOP, 0, 30);
			toast.show();
		}
	}

	/**
	 * 输出长时间toast
	 */
	public static void showLongToast(Context context, String str) {
		if (context != null) {
			Toast toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
			// 可以控制toast显示的位置
			toast.setGravity(Gravity.TOP, 0, 30);
			toast.show();
		}
	}

	/**
	 * 获取APP版本名
	 */
	public static String getVersionName(Context context) {
		try {
			String versionName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 镜像旋转
	 */
	public static Bitmap convert(Bitmap bitmap, boolean mIsFrontalCamera) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap newbBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newbBitmap);
		Matrix m = new Matrix();
		// m.postScale(1, -1); //镜像垂直翻转
		if (mIsFrontalCamera) {
			m.postScale(-1, 1); // 镜像水平翻转
		}
		// m.postRotate(-90); //旋转-90度
		Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
		cv.drawBitmap(bitmap2,
				new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight()),
				new Rect(0, 0, w, h), null);
		return newbBitmap;
	}

	/**
	 * 保存bitmap至指定Picture文件夹
	 */
	public static String saveBitmap(Context mContext, Bitmap bitmaptosave) {
		if (bitmaptosave == null)
			return null;

		File mediaStorageDir = mContext.getExternalFilesDir("megvii");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		// String bitmapFileName = System.currentTimeMillis() + ".jpg";
		String bitmapFileName = System.currentTimeMillis() + "";
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(mediaStorageDir + "/" + bitmapFileName);
			boolean successful = bitmaptosave.compress(
					Bitmap.CompressFormat.JPEG, 75, fos);

			if (successful)
				return mediaStorageDir.getAbsolutePath() + "/" + bitmapFileName;
			else
				return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
