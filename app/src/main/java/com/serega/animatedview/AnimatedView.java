package com.serega.animatedview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class AnimatedView extends SurfaceView implements SurfaceHolder.Callback {
	private DrawThread drawThread;
	private static final int EMPTY_VALUE = -1;
	private int squaresHorizontal = Constants.DEFAULT_SQUARES_COUNT_HORIZONTAL;
	private int marginBetween;
	private int flipSpeed = Constants.DEFAULT_FLIP_SPEED;
	private int backgroundColor = Constants.DEFAULT_BACKGROUND_COLOR;
	private long maxDelayValue;
	private int bitmapFront = EMPTY_VALUE;
	private int bitmapBack = EMPTY_VALUE;
	private boolean needAnimation = true;
	private boolean showProgressBar = true;
	private Future<Collection<Square>> futurePrepare;
	private PrepareProgressCallback prepareCallback;
	private PrepareUtils utils;

	interface PrepareProgressCallback {
		void onPrepareStart();

		void onPrepareFinish();
	}

	public AnimatedView(Context context) {
		super(context);
	}

	public AnimatedView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AnimatedView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(21)
	public AnimatedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
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
				size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constants.DEFAULT_SIZE_DP, getContext().getResources().getDisplayMetrics());
				break;
		}
		setMeasuredDimension(size, size);

	}

	void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		getHolder().addCallback(this);
		if (attrs != null) {
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AnimatedContainer, defStyleAttr, defStyleRes);
			squaresHorizontal = a.getInt(R.styleable.AnimatedContainer_squaresCountHorizontal, Constants.DEFAULT_SQUARES_COUNT_HORIZONTAL);
			marginBetween = a.getDimensionPixelSize(R.styleable.AnimatedContainer_marginBetweenSquares, 0);
			flipSpeed = a.getInt(R.styleable.AnimatedContainer_flipSpeed, Constants.DEFAULT_FLIP_SPEED);
			backgroundColor = a.getColor(R.styleable.AnimatedContainer_backgroundColor, Constants.DEFAULT_BACKGROUND_COLOR);
			maxDelayValue = a.getInt(R.styleable.AnimatedContainer_maxDelayValueInMillis, EMPTY_VALUE);
			bitmapFront = a.getResourceId(R.styleable.AnimatedContainer_drawableFront, EMPTY_VALUE);
			bitmapBack = a.getResourceId(R.styleable.AnimatedContainer_drawableBack, EMPTY_VALUE);
			needAnimation = a.getBoolean(R.styleable.AnimatedContainer_animateBitmapChange, true);
			a.recycle();
		}
	}

	void setPrepareCallback(PrepareProgressCallback callback) {
		prepareCallback = callback;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (prepareCallback != null) {
			prepareCallback.onPrepareStart();
		}
		utils = new PrepareUtils(getContext());
		utils.setViewWidth(getWidth())
				.setFrontBitmap(bitmapFront)
				.setBackBitmap(bitmapBack)
				.setSquaresCountHorizontal(squaresHorizontal)
				.setMarginBetween(marginBetween)
				.setFlipSpeed(flipSpeed)
				.useAnimation(needAnimation)
				.setMaxDelayValue(maxDelayValue);
		futurePrepare = utils.prepareAsync();
		drawThread = new DrawThread();
		drawThread.setRunning(true);
		drawThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		drawThread.setRunning(false);
		if (!futurePrepare.isDone() && !futurePrepare.isCancelled()) {
			futurePrepare.cancel(true);
		}
		while (retry) {
			try {
				drawThread.join();
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void flip() {
		drawThread.rotate();
	}

	private class DrawThread extends Thread {
		private final Paint squarePaint = new Paint();
		private boolean isRunning;
		private Collection<Square> squareList;
		private boolean needUpdateList;

		private DrawThread() {
			squarePaint.setColor(Color.RED);
		}

		private void rotate() {
			for (Square s : squareList) {
				s.randomizeStartDelay();
				s.flip();
			}
		}

		private void setRunning(boolean running) {
			isRunning = running;
		}

		private void needUpdateList(boolean needUpdateList) {
			this.needUpdateList = needUpdateList;
		}

		@Override
		public void run() {
			super.run();
			while (!futurePrepare.isDone()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				squareList = futurePrepare.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

			hideProgressBar();

			SurfaceHolder holder = getHolder();

			while (isRunning) {
				if (needUpdateList) {
					try {
						squareList = futurePrepare.get();
						needUpdateList = false;
						rotate();
						hideProgressBar();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
				Canvas canvas = holder.lockCanvas();
				if (canvas == null) {
					continue;
				}
				canvas.drawColor(backgroundColor);

				for (Square s : squareList) {
					s.draw(canvas);
				}
				holder.unlockCanvasAndPost(canvas);
			}
		}

		private Square.State currentState() {
			if (squareList != null) {
				for (Square s : squareList) {
					return s.getCurrentState();
				}
			}
			return Square.State.FRONT;
		}
	}

	void nextBitmap(int bitmapResourceId) {
		Square.State currentState = drawThread.currentState();
		switch (currentState) {
			case FRONT:
				bitmapBack = bitmapResourceId;
				utils.setBackBitmap(bitmapResourceId);
				utils.setInitialState(Square.State.FRONT);
				futurePrepare = utils.prepareAsync();
				break;

			case BACK:
				bitmapFront = bitmapResourceId;
				utils.setFrontBitmap(bitmapResourceId);
				utils.setInitialState(Square.State.BACK);
				futurePrepare = utils.prepareAsync();
				break;
		}
		showProgressBar();
		drawThread.needUpdateList(true);
	}

	void nextBitmap(Bitmap bitmap) {
		Square.State currentState = drawThread.currentState();
		switch (currentState) {
			case FRONT:
				utils.setBackBitmap(bitmap);
				utils.setInitialState(Square.State.FRONT);
				futurePrepare = utils.prepareAsync();
				break;

			case BACK:
				utils.setFrontBitmap(bitmap);
				utils.setInitialState(Square.State.BACK);
				futurePrepare = utils.prepareAsync();
				break;
		}
		showProgressBar();
		drawThread.needUpdateList(true);
	}

	private void showProgressBar(){
		if (showProgressBar && prepareCallback != null) {
			prepareCallback.onPrepareStart();
		}
	}

	private void hideProgressBar(){
		if (showProgressBar && prepareCallback != null) {
			prepareCallback.onPrepareFinish();
		}
	}

	void clear(){
		utils.setFrontBitmap(null);
		utils.setBackBitmap(null);
		futurePrepare = utils.prepareAsync();
		drawThread.needUpdateList(true);
	}
}
