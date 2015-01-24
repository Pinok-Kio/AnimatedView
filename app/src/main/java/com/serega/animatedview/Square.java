package com.serega.animatedview;

import android.graphics.*;

class Square {
	public static final float MAX_ANGLE_Y = 90;
	public static final float MAX_ANGLE_Z = 45;
	public static final int MAX_STEPS_COUNT = 180;
	private static final int DEFAULT_STEP = 6;
	private int animStep = DEFAULT_STEP;
	private final float translateCenterX;
	private final float translateCenterY;

	private final Rect rectMain;
	private float currentAngleX;
	private float currentAngleY;
	private float currentAngleZ;

	/**
	 * Delay before start flipping
	 */
	private long startDelay;
	private long maxDelay = Constants.DEFAULT_MAX_DELAY;

	/**
	 * If there's no bitmap, the rectMain will be painter in this color
	 */
	private int colorFront = Color.RED;

	/**
	 * If there's no bitmap, the rectMain will be painter in this color
	 */
	private int colorBack = Color.GREEN;
	private int stepsCount;

	private final Camera camera;
	private final Matrix matrix;
	private final Paint paint;

	private Bitmap bitmapFront;
	private Bitmap bitmapBack;
	private Bitmap currentBitmap;

	private boolean isInProgress;
	private boolean useAnimation = true;
	private State currentState = State.FRONT;

	public enum State {
		FRONT,
		BACK
	}

	Square(int topX, int topY, int width, int height) {
		rectMain = new Rect(topX, topY, topX + width, topY + height);

		translateCenterX = ((topX << 1) + width) / 2;
		translateCenterY = ((topY << 1) + width) / 2;

		camera = new Camera();
		matrix = new Matrix();

		paint = new Paint();
		paint.setColor(colorFront);
//		paint.setShadowLayer(15, 0, 10, Color.DKGRAY);
	}

	void useAnimation(boolean useAnimation) {
		this.useAnimation = useAnimation;
	}

	void setMaxDelay(long maxDelayInMillis) {
		maxDelay = maxDelayInMillis;
	}

	void randomizeStartDelay() {
		startDelay = System.currentTimeMillis() + (long) (Math.random() * maxDelay);
	}

	void setStartDelay(long startDelayInMillis) {
		startDelay = System.currentTimeMillis() + startDelayInMillis;
	}

	void setStep(int step) {
		this.animStep = step;
	}

	void setFrontColor(int colorFront) {
		this.colorFront = colorFront;
	}

	void setBackColor(int colorBack) {
		this.colorBack = colorBack;
	}

	void draw(Canvas canvas) {
		if (!isInProgress || System.currentTimeMillis() < startDelay) {
			if (currentBitmap != null) {
				canvas.drawBitmap(currentBitmap, null, rectMain, paint);
			} else {
				canvas.drawRect(rectMain, paint);
			}
			return;
		}
		drawWithRotate(canvas);
	}

	private void drawWithRotate(Canvas canvas) {
		canvas.save();
		if (useAnimation) {
			camera.save();
			camera.rotateY(currentAngleY);
			camera.rotateZ(currentAngleZ);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-translateCenterX, -translateCenterY);
			matrix.postTranslate(translateCenterX, translateCenterY);

			canvas.concat(matrix);
		}

		if (currentBitmap != null) {
			canvas.drawBitmap(currentBitmap, null, rectMain, paint);
		} else {
			canvas.drawRect(rectMain, paint);
		}

		canvas.restore();

		if (currentState == State.FRONT) {
			currentAngleY += animStep;
			currentAngleZ += animStep;
			stepsCount += animStep;

			if (currentAngleY == MAX_ANGLE_Y) {
				paint.setColor(colorBack);
				currentBitmap = bitmapBack;
				currentAngleY = -currentAngleY;
				currentAngleZ = -currentAngleZ;
			}
		} else {
			currentAngleY -= animStep;
			currentAngleZ -= animStep;
			stepsCount -= animStep;
			if (currentAngleY == -MAX_ANGLE_Y) {
				paint.setColor(colorFront);
				currentBitmap = bitmapFront;
				currentAngleY = -currentAngleY;
				currentAngleZ = -currentAngleZ;
			}
		}

		if (stepsCount >= MAX_STEPS_COUNT) {
			currentState = State.BACK;
			isInProgress = false;
		} else if (stepsCount <= 0) {
			currentState = State.FRONT;
			isInProgress = false;
		}
	}


	void flip() {
		isInProgress = true;
	}

	void setState(State state) {
		currentState = state;
		if (state == State.BACK) {
			currentBitmap = bitmapBack;
			stepsCount = MAX_STEPS_COUNT;
		} else {
			currentBitmap = bitmapFront;
			stepsCount = 0;
		}
	}

	int getWidth() {
		return rectMain.width();
	}

	int getHeight() {
		return rectMain.height();
	}

	void setBitmaps(Bitmap bitmapFront, Bitmap bitmapBack) {
		this.bitmapFront = bitmapFront;
		this.bitmapBack = bitmapBack;
		currentBitmap = bitmapFront;
	}

	void setFrontBitmap(Bitmap bitmapFront) {
		this.bitmapFront = bitmapFront;
		currentBitmap = bitmapFront;
	}

	void setBackBitmap(Bitmap bitmapBack) {
		this.bitmapBack = bitmapBack;
	}

	State getCurrentState() {
		return currentState;
	}
}
