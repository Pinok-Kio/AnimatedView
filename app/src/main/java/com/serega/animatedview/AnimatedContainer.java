package com.serega.animatedview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class AnimatedContainer extends FrameLayout implements AnimatedView.PrepareProgressCallback {
	private FrameLayout progress;
	private ProgressBar smallProgress;
	private Handler handler;
	private AnimatedView animatedView;
	public static final int DEFAULT_SIZE_DP = 150;
	private boolean firstStart = true;

	public AnimatedContainer(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public AnimatedContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public AnimatedContainer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	@TargetApi(21)
	public AnimatedContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int mode = MeasureSpec.getMode(widthMeasureSpec);
		int size = 0;
		switch (mode) {
			case MeasureSpec.EXACTLY:
				size = MeasureSpec.getSize(widthMeasureSpec);
				break;

			case MeasureSpec.AT_MOST:
				size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SIZE_DP, getContext().getResources().getDisplayMetrics());
				break;
		}
		setMeasuredDimension(size, size);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		handler = new Handler(Looper.getMainLooper());
		View view = LayoutInflater.from(context).inflate(R.layout.animated_view_layout, this, true);
		progress = (FrameLayout) view.findViewById(R.id.progress_bar);
		smallProgress = (ProgressBar) view.findViewById(R.id.progress_small);
		animatedView = (AnimatedView) view.findViewById(R.id.animatedView);
		animatedView.setPrepareCallback(this);
		animatedView.init(context, attrs, defStyleAttr, defStyleRes);
	}

	public void flip() {
		if (animatedView != null) {
			animatedView.flip();
		}
	}

	@Override
	public void onPrepareStart() {
		setProgress(View.VISIBLE);
	}

	@Override
	public void onPrepareFinish() {
		setProgress(View.GONE);
	}

	private void setProgress(final int visibility){
		if(Looper.getMainLooper().getThread().equals(Thread.currentThread())){
			if (smallProgress != null) {
				smallProgress.setVisibility(visibility);
				hideBigProgress(visibility);
			}
		}else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (smallProgress != null) {
						smallProgress.setVisibility(visibility);
						hideBigProgress(visibility);
					}
				}
			});
		}
	}

	private void hideBigProgress(int visibility){
		if(firstStart){
			if(progress != null && progress.getVisibility() == View.VISIBLE && visibility == View.GONE){
				progress.setVisibility(GONE);
				firstStart = false;
			}
		}
	}

	public void nextBitmap(int bitmapId){
		animatedView.nextBitmap(bitmapId);
	}

	public void nextBitmap(Bitmap bitmap){
		animatedView.nextBitmap(bitmap);
	}

	public void clear(){
		animatedView.clear();
	}

}
