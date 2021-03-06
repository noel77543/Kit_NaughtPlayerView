package tw.noel.sung.com.kit_naughtyplayerview.util.naughtyplayer.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.content.Context;
import android.media.AudioManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import tw.noel.sung.com.kit_naughtyplayerview.R;
import tw.noel.sung.com.kit_naughtyplayerview.util.naughtyplayer.controller.console.NaughtyPlayerConsole;
import tw.noel.sung.com.kit_naughtyplayerview.util.naughtyplayer.controller.header.NaughtyPlayerHeader;


/**
 * Created by noel on 2018/7/5.
 */

public class NaughtyPlayerController extends RelativeLayout implements View.OnTouchListener, Runnable, View.OnClickListener {

    private final int _ANIMATION_TIME = 400;
    private final float _MAX_ALPHA_VALUE = 1;
    private final float _MIN_ALPHA_VALUE = 0;
    private float currentAlpha = _MIN_ALPHA_VALUE;

    private float lastX;
    private float lastY;
    private float previousY;

    //允許值
    private int _DEFAULT_VALUE = 40;
    private Context context;
    private float viewHeight;
    private float viewWidth;

    //音量服務
    private AudioManager audioManager;
    //裝置音量最大值
    private int maxVolume;
    //裝置目前音量
    private int currentVolume;

    private View view;
    private ProgressBar verticalProgressBar;
    private ImageView imageView;

    private NaughtyPlayerHeader naughtyPlayerHeader;
    private NaughtyPlayerConsole naughtyPlayerConsole;

    //避免TOUCH
    private boolean isSliding;
    //是否播放中
    public boolean isPlaying;
    //是否成功初始化
    public boolean isReady;


    //-------
    public NaughtyPlayerController(Context context, NaughtyPlayerHeader naughtyPlayerHeader, NaughtyPlayerConsole naughtyPlayerConsole, View view) {
        super(context);
        this.context = context;
        this.naughtyPlayerHeader = naughtyPlayerHeader;
        this.naughtyPlayerConsole = naughtyPlayerConsole;
        this.view = view;

        this.view.setVisibility(GONE);
        verticalProgressBar = (ProgressBar) this.view.findViewById(R.id.vertical_progressbar);
        imageView = (ImageView) this.view.findViewById(R.id.image_view);
        init();
    }

    //-----------

    /***
     *  初始
     */
    private void init() {
        naughtyPlayerHeader.setVisibility(INVISIBLE);
        naughtyPlayerConsole.setVisibility(INVISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
        setLayoutTransition(new LayoutTransition());
        setBackgroundColor(context.getResources().getColor(R.color.controller_bg));
        setAlpha(currentAlpha);
        setOnClickListener(this);
        setOnTouchListener(this);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        post(this);
    }

    //------------------


    @Override
    public void run() {
        viewHeight = getHeight();
        viewWidth = getWidth();
    }

    //------

    /***
     *  顯示 / 隱藏 上方Header 與下方console
     */
    @Override
    public void onClick(View view) {

        if (!isReady) {
            Toast.makeText(context, context.getString(R.string.loading_error), Toast.LENGTH_SHORT).show();
            return;
        }

        //播放中按下 顯示標題與控制台
        if (isPlaying) {
            showHeader();
            showConsole();
        }
        //暫停時按下 隱藏標題與控制台
        else {
            hideHeader();
            hideConsole();
        }
        isPlaying = !isPlaying;
    }

    //----------------

    /***
     * 顯示header
     */
    private void showHeader() {
        naughtyPlayerHeader.setVisibility(View.VISIBLE);
        naughtyPlayerHeader
                .animate()
                .alpha(1)
                .setDuration(_ANIMATION_TIME)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        naughtyPlayerHeader.setVisibility(View.VISIBLE);
                    }
                });
    }

    //------------------

    /***
     *  隱藏 Header
     */
    private void hideHeader() {
        naughtyPlayerHeader
                .animate()
                .alpha(0)
                .setDuration(_ANIMATION_TIME)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        naughtyPlayerHeader.setVisibility(View.INVISIBLE);
                    }
                });
    }
    //------------------

    /***
     *  顯示console
     */
    private void showConsole() {
        naughtyPlayerConsole.setVisibility(View.VISIBLE);
        naughtyPlayerConsole
                .animate()
                .alpha(1)
                .setDuration(_ANIMATION_TIME)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        naughtyPlayerConsole.setVisibility(View.VISIBLE);
                    }
                });
    }

    //-----------------

    /***
     *  隱藏 Console
     */
    private void hideConsole() {
        naughtyPlayerConsole
                .animate()
                .alpha(0)
                .setDuration(_ANIMATION_TIME)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        naughtyPlayerConsole.setVisibility(View.INVISIBLE);
                    }
                });
    }

    //----------------

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float currentX = motionEvent.getX();
        float currentY = motionEvent.getY();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSliding = false;
                lastX = currentX;
                lastY = currentY;
                previousY = currentY;
                break;
            case MotionEvent.ACTION_MOVE:

                if (isVerticalSlide(lastX, currentX)) {
                    //Y當前位移值
                    float moveValueY = lastY - currentY;

                    //當 由上往下時  方向改變為 由下往上
                    if (currentY > lastY && previousY > currentY) {
                        lastY = previousY;
                    }
                    //當 由下往上時  方向改變為 由上往下
                    else if (currentY < lastY && previousY < currentY) {
                        lastY = previousY;
                    }
                    //記錄行為後的currentY 做為下次判斷前一次Y所在位置用
                    previousY = currentY;

                    this.view.setVisibility(VISIBLE);
                    //在螢幕左側 亮度調整
                    if (lastX < viewWidth / 2) {
                        moveValueY = moveValueY / viewHeight / 5;
                        currentAlpha = (currentAlpha - moveValueY) > _MAX_ALPHA_VALUE ? _MAX_ALPHA_VALUE : (currentAlpha - moveValueY) < _MIN_ALPHA_VALUE ? _MIN_ALPHA_VALUE : (currentAlpha - moveValueY);
                        setAlpha(currentAlpha);

                        if (!isSliding) {
                            //因為alpha最大值是1,故這邊將progress與max都乘以一千
                            verticalProgressBar.setMax((int) (_MAX_ALPHA_VALUE * 1000));
                            imageView.setBackgroundResource(R.drawable.ic_brightness);
                        }
                        verticalProgressBar.setProgress((int) ((1 - currentAlpha) * 1000));
                    }
                    //螢幕右側 聲音調整
                    else {
                        moveValueY = moveValueY / maxVolume / 10;
                        currentVolume = (int) ((currentVolume + moveValueY) > maxVolume ? maxVolume : (currentVolume + moveValueY) < 0 ? 0 : (currentVolume + moveValueY));
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                        if (!isSliding) {
                            verticalProgressBar.setMax(maxVolume);
                            imageView.setBackgroundResource(R.drawable.ic_volume);
                        }
                        verticalProgressBar.setProgress(currentVolume);
                    }

                    isSliding = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                this.view.setVisibility(GONE);
                //如果觸發MOVE僅是為了設定 此時return true 避免事件到下層onclickListener
                return isSliding;
        }

        return false;
    }

    //------------

    /***
     * 垂直滑動-
     * 如果X變動值在允許值以下 表示垂直移動
     * @return
     */
    private boolean isVerticalSlide(float lastX, float currentX) {
        return Math.abs((int) (lastX - currentX)) < _DEFAULT_VALUE;
    }
}
