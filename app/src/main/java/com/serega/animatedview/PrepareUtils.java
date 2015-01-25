package com.serega.animatedview;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class PrepareUtils {
	private static final int EMPTY_VALUE = -1;
	/**
	 * AnimatedView width
	 */
	private int width = EMPTY_VALUE;

	/**
	 * If there's bitmap exists, that bitmap will be drawn on top
	 */
	private int bitmapFrontId = EMPTY_VALUE;

	/**
	 * If there's bitmap exists, that bitmap will be drawn after view flipped
	 */
	private int bitmapBackId = EMPTY_VALUE;

	/**
	 * If there's bitmap exists, that bitmap will be drawn on top.
	 * It bitmapFrontId != -1, this bitmap will not drawn
	 */
	private Bitmap bitmapFront;

	/**
	 * If there's bitmap exists, that bitmap will be drawn after view flipped.
	 * It bitmapBackId != -1, this bitmap will not drawn
	 */
	private Bitmap bitmapBack;

	/**
	 * Define how many regions will be used in one line (squaresHorizontal * squaresHorizontal = full count)
	 */
	private int squaresHorizontal = Constants.DEFAULT_SQUARES_COUNT_HORIZONTAL;

	/**
	 * Margins between regions
	 */
	private int marginBetween;

	/**
	 * Flip animation speed
	 */
	private int flipSpeed = Constants.DEFAULT_FLIP_SPEED;

	/**
	 * Max delay before flip animation started
	 */
	private long maxDelayValue = EMPTY_VALUE;
	private Square.State initialState = Square.State.FRONT;

	/**
	 * Should we use rotate animation?
	 */
	private boolean animateChanges = true;

	private boolean computeCurrentBitmap;
	private boolean computeNextBitmap;
	private final Context context;

	PrepareUtils(Context context) {
		this.context = context;
	}

	/**
	 * Set AnimatedView width
	 *
	 * @param width AnimatedView width
	 * @return reference to self
	 */
	PrepareUtils setViewWidth(int width) {
		this.width = width;
		return this;
	}

	/**
	 * Set bitmap to draw in front of AnimatedView
	 *
	 * @param id bitmap resource id
	 * @return reference to self
	 */
	PrepareUtils setFrontBitmap(int id) {
		bitmapFrontId = id;
		return this;
	}

	/**
	 * Set bitmap to draw in front of AnimatedView
	 *
	 * @param bitmap bitmap to use
	 * @return reference to self
	 */
	PrepareUtils setFrontBitmap(Bitmap bitmap) {
		bitmapFrontId = EMPTY_VALUE;
		bitmapFront = bitmap;
		return this;
	}

	/**
	 * Set bitmap to draw after AnimatedView if plipped
	 *
	 * @param id bitmap resource id
	 * @return reference to self
	 */
	PrepareUtils setBackBitmap(int id) {
		bitmapBackId = id;
		return this;
	}

	/**
	 * Set bitmap to draw after AnimatedView if plipped
	 *
	 * @param bitmap bitmap to use
	 * @return reference to self
	 */
	PrepareUtils setBackBitmap(Bitmap bitmap) {
		bitmapBackId = EMPTY_VALUE;
		bitmapBack = bitmap;
		return this;
	}

	/**
	 * Set how many squares should be in one line
	 *
	 * @param count squares count
	 * @return reference to self
	 */
	PrepareUtils setSquaresCountHorizontal(int count) {
		squaresHorizontal = count;
		return this;
	}

	/**
	 * Set margins between squares
	 *
	 * @param margin margin between squares
	 * @return reference to self
	 */
	PrepareUtils setMarginBetween(int margin) {
		marginBetween = margin;
		return this;
	}

	/**
	 * Set flip animation speed
	 *
	 * @param speed flip animation speed, see values in attrs
	 * @return reference to self
	 */
	PrepareUtils setFlipSpeed(int speed) {
		flipSpeed = speed;
		return this;
	}

	/**
	 * Set max delay before rotate animation started
	 *
	 * @param delay max delay value
	 * @return reference to self
	 */
	PrepareUtils setMaxDelayValue(long delay) {
		maxDelayValue = delay;
		return this;
	}

	PrepareUtils setInitialState(Square.State state) {
		initialState = state;
		return this;
	}

	PrepareUtils computeBitmaps(boolean current, boolean next) {
		computeCurrentBitmap = current;
		computeNextBitmap = next;
		return this;
	}

	/**
	 * Is rotate animation required?
	 *
	 * @param animateChanges true - use animation (default), false - no animation
	 * @return reference to self
	 */
	PrepareUtils useAnimation(boolean animateChanges) {
		this.animateChanges = animateChanges;
		return this;
	}

	/**
	 * Prepare Squared (dividing bitmaps, set initial values). Take some time, should run outside main thread.
	 *
	 * @return collection of Squares to use
	 * @throws IOException if the image format is not supported or can not be decoded.
	 */
	Collection<Square> prepare() throws IOException {
		checkWidth();
		int colorFront = 0;
		int colorBack = 0;

		int squaresAreaDimen = (width - marginBetween) / squaresHorizontal;
		int imax = squaresHorizontal * squaresHorizontal;
		Collection<Square> squareList = new ArrayList<>(imax);

		List<Bitmap> frontBitmaps = getBitmap(bitmapFrontId, bitmapFront);
		BitmapRegionDecoder decoderFront = makeDecoder(frontBitmaps);

		List<Bitmap> toCacheFront = null;
		if (decoderFront == null) {
			colorFront = context.getResources().getColor(R.color.holo_red_dark);
		}else{
			toCacheFront = new ArrayList<>(imax);
		}

		List<Bitmap> backBitmaps = getBitmap(bitmapBackId, bitmapBack);
		BitmapRegionDecoder decoderBack = makeDecoder(backBitmaps);

		List<Bitmap> toCacheBack = null;
		if (decoderBack == null) {
			colorBack = context.getResources().getColor(R.color.holo_green_dark);
		}else{
			toCacheBack = new ArrayList<>(imax);
		}

		for (int i = 0, j = 0, k = 0; i < imax; i++) {
			int startX = j * squaresAreaDimen + marginBetween;
			int startY = k * squaresAreaDimen + marginBetween;

			Square s = new Square(startX, startY, squaresAreaDimen - marginBetween, squaresAreaDimen - marginBetween);

			Rect bitmapRegion = new Rect(startX - marginBetween, startY - marginBetween,
					startX + s.getWidth(), startY + s.getHeight());

			setSquareBitmaps(s, decoderFront, decoderBack, bitmapRegion, frontBitmaps, backBitmaps,
					toCacheFront, toCacheBack, colorFront, colorBack, i);

			s.setStep(flipSpeed);
			if (maxDelayValue != EMPTY_VALUE) {
				s.setMaxDelay(maxDelayValue);
			}
			s.setState(initialState);
			s.useAnimation(animateChanges);
			squareList.add(s);
			j++;
			if (j == squaresHorizontal) {
				j = 0;
				k++;
			}
		}

		if (toCacheFront != null) {
			int frontKey = (bitmapFront != null) ? bitmapFront.hashCode() : bitmapFrontId;
			Cache.getInstance().put(frontKey, toCacheFront);
		}
		if (toCacheBack != null) {
			int backKey = (bitmapBack != null) ? bitmapBack.hashCode() : bitmapBackId;
			Cache.getInstance().put(backKey, toCacheBack);
		}

		return squareList;
	}

	private void checkWidth() {
		if (width == EMPTY_VALUE) {
			width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constants.DEFAULT_SIZE_DP, context.getResources().getDisplayMetrics());
		}
	}

	private static void setSquareBitmaps(Square s, BitmapRegionDecoder decoderFront, BitmapRegionDecoder decoderBack, Rect bitmapRegion,
	                                     List<Bitmap> frontBitmaps, List<Bitmap> backBitmaps, Collection<Bitmap> toCacheFront, Collection<Bitmap> toCacheBack,
	                                     int colorFront, int colorBack, int index){
		if (decoderFront != null) {
			Bitmap squareFrontBitmap = decoderFront.decodeRegion(bitmapRegion, null);
			s.setFrontBitmap(squareFrontBitmap);
			toCacheFront.add(squareFrontBitmap);
		} else if (frontBitmaps != null) {
			s.setFrontBitmap(frontBitmaps.get(index));
		} else {
			s.setFrontColor(colorFront);
		}

		if (decoderBack != null) {
			Bitmap squareBackBitmap = decoderBack.decodeRegion(bitmapRegion, null);
			s.setBackBitmap(squareBackBitmap);
			toCacheBack.add(squareBackBitmap);
		} else if (backBitmaps != null) {
			s.setBackBitmap(backBitmaps.get(index));
		} else {
			s.setBackColor(colorBack);
		}
	}

	@Nullable
	private List<Bitmap> getBitmap(int bitmapId, Bitmap bitmap) {
		List<Bitmap> result = getFromCache(bitmapId, bitmap);

		if(result != null){
			return result;
		}

		if (bitmapId != EMPTY_VALUE) {
			result = new ArrayList<>(1);
			result.add(bitmapFromResources(bitmapId));
			return result;
		}

		if(bitmap != null) {
			result = new ArrayList<>(1);
			result.add(scaleBitmap(bitmap, width));
			return result;
		}

		return null;
	}

	@Nullable
	private static List<Bitmap> getFromCache(int bitmapId, Bitmap bitmap) {
		if (bitmapId != EMPTY_VALUE) {
			return Cache.getInstance().get(bitmapId);
		}
		if(bitmap != null) {
			return Cache.getInstance().get(bitmap.hashCode());
		}
		return null;
	}

	@Nullable
	private Bitmap bitmapFromResources(int bitmapId) {
		Bitmap b = BitmapFactory.decodeResource(context.getResources(), bitmapId);
		if (b != null) {
			return scaleBitmap(b, width);
		}
		return null;
	}

	@Nullable
	private static BitmapRegionDecoder makeDecoder(List<Bitmap> bitmaps) throws IOException {
		if(bitmaps != null && bitmaps.size() == 1){
			Bitmap bitmap = bitmaps.get(0);
			byte[] array = bitmapToByteArray(bitmap);
			bitmap.recycle();
			return BitmapRegionDecoder.newInstance(array, 0, array.length, true);
		}
		return null;
	}

	private static Bitmap scaleBitmap(Bitmap bitmap, int width) {
		return Bitmap.createScaledBitmap(bitmap, width, width, false);
	}

	private static byte[] bitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		return out.toByteArray();
	}

	/**
	 * Prepare Squared asynchronously(dividing bitmaps, set initial values).
	 *
	 * @return future with collection of Squares to use
	 */
	public Future<Collection<Square>> prepareAsync() {
		return Executors.newSingleThreadExecutor().submit(new Callable<Collection<Square>>() {
			@Override
			public Collection<Square> call() throws Exception {
				return prepare();
			}
		});
	}
}
