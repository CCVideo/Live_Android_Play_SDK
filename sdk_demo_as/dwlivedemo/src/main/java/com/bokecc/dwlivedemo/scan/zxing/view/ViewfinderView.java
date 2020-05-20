/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bokecc.dwlivedemo.scan.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.scan.zxing.camera.CameraManager;
import com.google.zxing.ResultPoint;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 */
public final class ViewfinderView extends View {
	
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;

	
	private int ScreenRate;
	
	
	private static final int CORNER_WIDTH = 5;
	
	private static final int MIDDLE_LINE_WIDTH = 6;
	
	
	private static final int MIDDLE_LINE_PADDING = 5;
	
	
	private static final int SPEEN_DISTANCE = 5;
	
	
	private static float density;
	
	private static final int TEXT_SIZE = 13;
	/**
	 */
	private static final int TEXT_PADDING_TOP = 30;
	
	/**
	 */
	private Paint paint;
	
	/**
	 */
	private int slideTop;
	private int firstSlideTop;
	
	/**
	 */
	private Bitmap resultBitmap;
	private int maskColor;
//	private int resultColor;
	
	private int resultPointColor;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;

	boolean isFirst;

	public ViewfinderView(Context context) {
		super(context);
		initColor();
	}

	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		density = context.getResources().getDisplayMetrics().density;
		ScreenRate = (int)(15 * density);
		initColor();
	}

	private void initColor() {
		paint = new Paint();
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
//		resultColor = resources.getColor(R.color.result_view);

		resultPointColor = resources.getColor(R.color.possible_result_points);
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initColor();
	}

	@Override
	public void onDraw(Canvas canvas) {
		Rect frame = CameraManager.get().getFramingRect();
		if (frame == null) {
			return;
		}
		
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
		frame.left = width / 5;
		frame.right = width * 4 /5;

		frame.bottom = height / 2 + width * 3 / 10;
		frame.top = height / 2 - width * 3 / 10;
		
//		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		paint.setColor(maskColor);

		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
		canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
		canvas.drawRect(0, frame.bottom, width, height, paint);
		
		

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {

			paint.setColor(Color.rgb(255, 64, 0));
			canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate, frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top + ScreenRate, paint);

			canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right, frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top + ScreenRate, paint);

			canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left + ScreenRate, frame.bottom, paint);
			canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left + CORNER_WIDTH, frame.bottom, paint);

			canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH, frame.right, frame.bottom, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate, frame.right, frame.bottom, paint);

			if(!isFirst){
				isFirst = true;
				slideTop = frame.top;
				if (slideTop > 0) {
					firstSlideTop = slideTop;
				}
			}

			// 由于魅族的bug，导致这里会有负值，故这里需要重新处理
			if (slideTop < 0) {
				slideTop = firstSlideTop;
			}
			slideTop += SPEEN_DISTANCE;
			if(slideTop >= frame.bottom){
				slideTop = frame.top;
			}

			Rect lineRect = new Rect();
            lineRect.left = frame.left + 3;
            lineRect.right = frame.right - 3 ;
            lineRect.top = slideTop;  
            lineRect.bottom = slideTop + 8;

            canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.qrcode_scan_line))).getBitmap(), null, lineRect, paint);
			
            paint.setColor(Color.WHITE);    
            paint.setTextSize(TEXT_SIZE * density);    
            paint.setAlpha(0x40);    
//            paint.setTypeface(Typeface.create("System", Typeface.BOLD));
            String text = getResources().getString(R.string.scan_text);
            float textWidth = paint.measureText(text);  
              
            canvas.drawText(text, (width - textWidth)/2, (float) (frame.bottom + (float)TEXT_PADDING_TOP * density), paint);
			
			

			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
				}
			}

			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
				}
			}

			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
			
		}
	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

}
