package com.shuyi.chenxiao;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.shuyi.chenxiao.adapter.MyAdapter;
import com.shuyi.chenxiao.pupup.PopupChoisePhoto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author：半世晨晓i
 * Time：2018/1/16
 * Email：shuyint@aliyin.com
 * Name：View
 * Describe:相机拍照&相册选择&简易预览
 */

public class MainActivity extends Activity {

    //显示图片的GridView
    private GridView mGridView;
    //GridView Adapter
    private MyAdapter mMyAdapter;
    //判断是添加新照片还是更新已有照片
    private boolean isAdd = true;
    //数据源初始长度
    private int photosSize = 0;
    //选择哪张照片
    private int whichPhoto = 0;
    //相册请求码
    private static final int ALBUM_REQUEST_CODE = 1;
    //相机请求码
    private static final int CAMERA_REQUEST_CODE = 2;
    //调用照相机返回图片文件
    private File cameraFile;
    // 保存下当前动画类，以便可以随时结束动画
    private Animator mCurrentAnimator;

    //系统的短时长动画持续时间（单位ms）
    // 对于不易察觉的动画或者频繁发生的动画
    // 这个动画持续时间是最理想的
    private int mShortAnimationDuration;

    private LinearLayout parentLaoyout;

    private ArrayList<Bitmap> bitmap_list = new ArrayList<>();//图片集合

    private PopupChoisePhoto choisePhotoPopup;//选择相机or图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //取回系统默认的短时长动画持续时间
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        initView();
    }


    private void initView() {
        mGridView = findViewById(R.id.img_add_grid);
        mMyAdapter = new MyAdapter(MainActivity.this, bitmap_list);
        mGridView.setAdapter(mMyAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position < bitmap_list.size()) {
                    //更新
                    isAdd = false;
                    whichPhoto = position;
                    photosSize=bitmap_list.size();
                } else {
                    //添加
                    whichPhoto = position;
                    photosSize=bitmap_list.size();
                    isAdd = true;
                }
                showChoisePhoto();
            }
        });
    }

    //选择相机or图片
    private void showChoisePhoto() {
        parentLaoyout = findViewById(R.id.add_main);
        choisePhotoPopup = new PopupChoisePhoto(MainActivity.this, choisePhotoCilck, parentLaoyout, whichPhoto,photosSize);
        choisePhotoPopup.showPopupWindow(parentLaoyout);
        parentLaoyout.setAlpha((float) 0.3);
    }


    private android.view.View.OnClickListener choisePhotoCilck = new android.view.View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.takePhoto:
                    //打开照相机
                    takePhoto();
                    break;
                case R.id.choosePhoto:
                    //打开相册
                    choosePhoto();
                    break;
                case R.id.btn_del_select:
                    if (bitmap_list.size() != 0) {
                        bitmap_list.remove(whichPhoto);
                        mMyAdapter.notifyDataSetChanged();
                        choisePhotoPopup.dismissPopupWindow(parentLaoyout);
                    } else {
                        Toast.makeText(MainActivity.this, "暂无图片,请先添加图片", Toast.LENGTH_SHORT).show();
                        choisePhotoPopup.dismissPopupWindow(parentLaoyout);
                    }
                    break;
                case R.id.btn_img_zoom:
                    //图片预览
                    zoomImageFromThumb(mGridView, bitmap_list.get(whichPhoto));
                    choisePhotoPopup.dismissPopupWindow(parentLaoyout);
                    break;
                case R.id.btn_cancel:
                    //取消
                    choisePhotoPopup.dismissPopupWindow(parentLaoyout);
                    break;

                default:
                    break;
            }
        }
    };


    /**
     * 获取相机
     */
    private void takePhoto() {
        //用于保存调用相机拍照后所生成的文件
        cameraFile = new File(Environment.getExternalStorageDirectory().getPath(), System.currentTimeMillis() + ".jpg");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(MainActivity.this, "com.shuyi.chenxiao", cameraFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
        }
        startActivityForResult(intent, CAMERA_REQUEST_CODE);

    }

    /**
     * 获取相册
     */
    private void choosePhoto() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, ALBUM_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:   //调用相机后返回
                //是否正常拍照
                if (resultCode == RESULT_OK) {
                    //正常拍照
                    //压缩图片
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    try {
                        Bitmap oldBitMap = BitmapFactory.decodeFile(cameraFile.getCanonicalPath());
                        Bitmap newBitMap = BitmapFactory.decodeFile(cameraFile.getCanonicalPath(), options);
                        Log.i("wechat", "压缩前图片的大小" + (oldBitMap.getByteCount() / 1024 / 1024 / 16)
                                + "M宽度为" + oldBitMap.getWidth() + "高度为" + oldBitMap.getHeight());
                        Log.i("wechat", "压缩后图片的大小" + (newBitMap.getByteCount() / 1024 / 1024 / 16)
                                + "M宽度为" + newBitMap.getWidth() + "高度为" + newBitMap.getHeight());
                        //内存回收
                        //oldBitMap.recycle();废弃方法
                        //newBitMap.recycle();废弃方法
                        //判断是添加照片还是更新照片
                        if (isAdd) {
                            bitmap_list.add(newBitMap);
                        } else {
                            if (photosSize < bitmap_list.size()) {
                                bitmap_list.set(whichPhoto, newBitMap);
                            }
                        }
                        //刷新数据  关闭popupwidow
                        mMyAdapter.notifyDataSetChanged();
                        choisePhotoPopup.dismissPopupWindow(parentLaoyout);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode ==RESULT_CANCELED){
                    //取消拍照
                    choisePhotoPopup.dismissPopupWindow(parentLaoyout);
                }

                break;
            case ALBUM_REQUEST_CODE:    //调用相册后返回
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    //得到bitmap图片
                    //ContentResolver resolver = getContentResolver();
                    // try {
                    // Bitmap  bitmapPhoto=MediaStore.Images.Media.getBitmap(resolver, uri);
                    //Log.i("BitMapPhoto", "压缩前图片的大小" + (bitmapPhoto.getByteCount() / 1024 / 1024 / 16)
                    //        + "M宽度为" + bitmapPhoto.getWidth() + "高度为" + bitmapPhoto.getHeight());
                    // bitmapPhoto.recycle();
                    //} catch (IOException e) {
                    //e.printStackTrace();
                    //}
                    //图片裁剪
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;

                    //获取图片的路径：
                    String[] photoPath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(uri, photoPath, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    Bitmap newBitMapPhoto = BitmapFactory.decodeFile(path, options);
                    //newBitMapPhoto.recycle();
                    Log.i("newBitMapPhoto", "压缩后图片的大小" + (newBitMapPhoto.getByteCount() / 1024 / 1024 / 16)
                            + "M宽度为" + newBitMapPhoto.getWidth() + "高度为" + newBitMapPhoto.getHeight());
                    //判断是添加照片还是更新照片
                    if (isAdd) {
                        bitmap_list.add(newBitMapPhoto);
                    } else {
                        if (photosSize < bitmap_list.size()) {
                            bitmap_list.set(whichPhoto, newBitMapPhoto);
                        }
                    }
                    //刷新数据  关闭popupwidow
                    mMyAdapter.notifyDataSetChanged();
                    choisePhotoPopup.dismissPopupWindow(parentLaoyout);
                } else if (resultCode ==RESULT_CANCELED){
                    choisePhotoPopup.dismissPopupWindow(parentLaoyout);
                }
                break;
        }
    }

    private void zoomImageFromThumb(final View thumbView, Bitmap bitmap) {
        // 如果有动画在执行，立即取消，然后执行现在这个动画
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        //加载Drwable
        // zoomImageFromThumb(final View thumbView, int imageResId)
        //expandedImageView.setImageResource(imageResId);


        // 加载高分辨率的图片
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        expandedImageView.setImageBitmap(bitmap);

        // 计算开始和结束位置的图片范围
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // 开始的范围就是ImageButton的范围，
        // 结束的范围是容器（FrameLayout）的范围
        // getGlobalVisibleRect(Rect)得到的是view相对于整个硬件屏幕的Rect
        // 即绝对坐标，减去偏移，获得动画需要的坐标，即相对坐标
        // getGlobalVisibleRect(Rect,Point)中，Point获得的是view在它在
        // 父控件上的坐标与在屏幕上坐标的偏移
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.lin_photo)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).

        // 下面这段逻辑其实就是保持纵横比
        float startScale;
        // 如果结束图片的宽高比比开始图片的宽高比大
        // 就是结束时“视觉上”拉宽了（压扁了）图片
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.

        // 隐藏小的图片，展示大的图片。当动画开始的时候，
        // 要把大的图片发在小的图片的位置上

        //小的设置透明
        thumbView.setAlpha(0f);
        //大的可见
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.

        // 再次点击返回小的图片，就是上面扩大的反向动画。即预览完成
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
