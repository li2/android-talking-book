package com.douzi.android.lrc;


import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * LrcView can display LRC file and Seek it.
 * @author douzifly
 *
 */
/*
新增功能：
完成类似网易云音乐的「上下滚动歌词」功能。
「上下滚动歌词」满足用户查看「非当前正在播放处歌词」的需求，播放歌曲时「上下滚动歌词」后回滚到「当前正在播放处」
这篇文章对此有简单描述：http://zhuanlan.zhihu.com/appdetail/19604263

具体实现：
(1) “正常播放模式”下，“当前播放行”是高亮行，位与lrcview中部，只由音频的播放进度改变 seekLrcToTime(long time);
(2) 用户滚动lrcview时，进入“Seek模式”，显示SeekLine及SeekButton两个子view；
“用户选中行”位与lrcview中部，用浅灰色高亮区分；此时“当前播放行”仍未高两行，但已不在中部；
(3) 用户手指离开屏幕时，保持“seek模式”5s，超时后切换为“正常播放模式”, 此间若用户点击SeekButton则播放“用户选中行”。

weiyi.just2@gmail.com
li2.me
 */
public class LrcView extends View implements ILrcView{

	public final static String TAG = "LrcView";

	/** normal display mode*/
	public final static int DISPLAY_MODE_NORMAL = 0;
	/** seek display mode */
	public final static int DISPLAY_MODE_SEEK = 1;
	/** scale display mode ,scale font size*/
	public final static int DISPLAY_MODE_SCALE = 2;

	private final static int LRCVIEW_SEEK_MODEL_DELAY = 5000; // 5s
	
	private List<LrcRow> mLrcRows; 	// all lrc rows of one lrc file
	private int mMinSeekFiredOffset = 10; // min offset for fire seek action, px;
	private int mHignlightRow = 0;  // current singing row , should be highlighted.
	private int mHignlightRowColor = Color.BLUE;
	private int mNormalRowColor = Color.DKGRAY;
	private int mSeekedRow = 0;     // current seeked row.
	private int mSeekedRowColor = Color.LTGRAY;
	private int mSeekLineColor = Color.LTGRAY;
	private int mSeekLineTextColor = Color.LTGRAY;
	private int mSeekLineTextSize = 28;
	private int mMinSeekLineTextSize = 13;
	private int mMaxSeekLineTextSize = 18;
	private int mSeekLineTextWidth = 100;
	private int mSeekTriangleLength = 32;
	private int mLrcFontSize = 32; 	// font size of lrc
	private int mMinLrcFontSize = 15;
	private int mMaxLrcFontSize = 35;
	private int mPaddingY = 32;     // padding of each row
	private int mPaddingX = 32;     // horizontal padding of each row
	private int mSeekLinePaddingX = mSeekLineTextWidth+8; // Seek line padding x
	private int mDisplayMode = DISPLAY_MODE_NORMAL;
	private Align mLrcAlign = Align.LEFT;
	private LrcViewListener mLrcViewListener;
	private String mLoadingLrcTip = "Downloading lrc...";

	private TextPaint mPaint;
	private Handler mHandler;
	private Runnable mRunnableSwitchToNormal;

	public LrcView(Context context,AttributeSet attr){
		super(context,attr);
		mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextSize(mLrcFontSize);
		mHandler = new Handler();
		mRunnableSwitchToNormal = new Runnable() {
		    public void run() {
		        mDisplayMode = DISPLAY_MODE_NORMAL;
		    }
		};
	}

	public void setListener(LrcViewListener l){
		mLrcViewListener = l;
	}

	public void setLoadingTipText(String text){
		mLoadingLrcTip = text;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int height = getHeight(); // height of this view
		final int width = getWidth() ; // width of this view
		if(mLrcRows == null || mLrcRows.size() == 0){
			if(mLoadingLrcTip != null){
				// draw tip when no lrc.
				mPaint.setColor(mHignlightRowColor);
				mPaint.setTextSize(mLrcFontSize);
				mPaint.setTextAlign(mLrcAlign);
				canvas.drawText(mLoadingLrcTip, 0, height / 2 - mLrcFontSize, mPaint);
			}
			return;
		}

		StaticLayout highlightLayout;
		
		int rowY = 0; // vertical point of each row.
		final int rowX = width / 2;
		int rowNum = 0;

		// 1, draw highlight row at center.
		// 2, draw rows above highlight row.
		// 3, draw rows below highlight row.
		int centerRowY = height / 2;
		
		if(mDisplayMode == DISPLAY_MODE_SEEK){
		    // draw Seek line and current time when moving.
		    int seekTriangleX = mPaddingX/2;
		    int seekTextX = width - mSeekLineTextWidth - mPaddingX/2;
		    int seekLineX = seekTriangleX + mSeekTriangleLength + 16;
		    int seekLineW = seekTextX - seekLineX - 16;
		    String seekStr = mLrcRows.get(mSeekedRow).strTime.substring(0, 5);
		    drawSeekTriangle(canvas, seekTriangleX, height/2, mSeekTriangleLength);
		    drawSeekLine(canvas, seekLineX, height/2, seekLineW);
		    drawSeekLineText(canvas, seekTextX, height/2, mSeekLineTextWidth, seekStr);
			
	         // 1 highlight row
            String highlightText = mLrcRows.get(mSeekedRow).content;
            
            mPaint.setColor(mSeekedRowColor);
            mPaint.setTextSize(mLrcFontSize);
            mPaint.setTextAlign(mLrcAlign);
            
            highlightLayout = buildStaticLayout(highlightText, mPaint, canvas, mPaddingX, centerRowY);

            // 2 above rows
            mPaint.setColor(mNormalRowColor);
            mPaint.setTextSize(mLrcFontSize);
            mPaint.setTextAlign(mLrcAlign);
            rowNum = mSeekedRow - 1;
            rowY = centerRowY;
            while( rowY > -mLrcFontSize && rowNum >= 0){
                if (rowNum == mHignlightRow) {
                    mPaint.setColor(mHignlightRowColor);
                } else {
                    mPaint.setColor(mNormalRowColor);
                }
                String text = mLrcRows.get(rowNum).content;
                rowY -=  (getLineHeight(text, mPaint, canvas) + mPaddingY);
                StaticLayout aboveLayout = buildStaticLayout(text, mPaint, canvas, mPaddingX, rowY);
                rowNum --;
            }

            // 3 below rows
            rowNum = mSeekedRow + 1;
            rowY = centerRowY + highlightLayout.getHeight() + mPaddingY;
            while( rowY < height && rowNum < mLrcRows.size()){
                if (rowNum == mHignlightRow) {
                    mPaint.setColor(mHignlightRowColor);
                } else {
                    mPaint.setColor(mNormalRowColor);
                }
                String text = mLrcRows.get(rowNum).content;
                StaticLayout belowLayout = buildStaticLayout(text, mPaint, canvas, mPaddingX, rowY);
                rowY += (belowLayout.getHeight() + mPaddingY);
                rowNum ++;
            }
			
		} else {
	        // 1 highlight row
	        String highlightText = mLrcRows.get(mHignlightRow).content;
	        
	        mPaint.setColor(mHignlightRowColor);
	        mPaint.setTextSize(mLrcFontSize);
	        mPaint.setTextAlign(mLrcAlign);
	        
	        highlightLayout = buildStaticLayout(highlightText, mPaint, canvas, mPaddingX, centerRowY);

	        // 2 above rows
	        mPaint.setColor(mNormalRowColor);
	        mPaint.setTextSize(mLrcFontSize);
	        mPaint.setTextAlign(mLrcAlign);
	        rowNum = mHignlightRow - 1;
	        rowY = centerRowY;
	        while( rowY > -mLrcFontSize && rowNum >= 0){
	            String text = mLrcRows.get(rowNum).content;
	            rowY -=  (getLineHeight(text, mPaint, canvas) + mPaddingY);
	            StaticLayout aboveLayout = buildStaticLayout(text, mPaint, canvas, mPaddingX, rowY);
	            rowNum --;
	        }

	        // 3 below rows
	        rowNum = mHignlightRow + 1;
	        rowY = centerRowY + highlightLayout.getHeight() + mPaddingY;
	        while( rowY < height && rowNum < mLrcRows.size()){
	            String text = mLrcRows.get(rowNum).content;
	            StaticLayout belowLayout = buildStaticLayout(text, mPaint, canvas, mPaddingX, rowY);
	            rowY += (belowLayout.getHeight() + mPaddingY);
	            rowNum ++;
	        }
		}
	}

    // http://stackoverflow.com/questions/6756975/draw-multi-line-text-to-canvas
	private StaticLayout buildStaticLayout(String text, TextPaint paint, Canvas canvas, float x, float y) {
        StaticLayout textLayout = new StaticLayout(text, paint, (int)(canvas.getWidth()- x*2), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.save();
        canvas.translate(x, y);
        textLayout.draw(canvas);
        canvas.restore();
        Log.d(TAG, "line height " + textLayout.getHeight());
	    return textLayout;
	}
	
	private int getLineHeight(String text, TextPaint paint, Canvas canvas) {
	    StaticLayout textLayout = new StaticLayout(text, paint, canvas.getWidth()- mPaddingX*2, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
	    return textLayout.getHeight();
	}
	
	// http://stackoverflow.com/questions/20544668/how-to-draw-filled-triangle-on-android-canvas
	private void drawSeekTriangle(Canvas canvas, int x, int y, int length) {
	    Paint paint = new Paint();
	    paint.setStrokeWidth(4);
	    paint.setColor(Color.DKGRAY);
	    paint.setStyle(Paint.Style.FILL_AND_STROKE);
	    paint.setAntiAlias(true);
	    paint.setAlpha(111);
	    
	    Point a = new Point(x, y-mSeekTriangleLength/2);
	    Point b = new Point(x, y+mSeekTriangleLength/2);
	    Point c = new Point((int)(x+mSeekTriangleLength*0.866), y);
	    
	    Path path = new Path();
	    path.setFillType(FillType.EVEN_ODD);
	    path.moveTo(b.x, b.y);
	    path.lineTo(c.x, c.y);
	    path.lineTo(a.x, a.y);
	    path.close();
	    
	    canvas.drawPath(path, paint);
	}
	
	// draw Seek line and current time when moving.
	private void drawSeekLine(Canvas canvas, int x, int y, int width) {
	    TextPaint paint = new TextPaint();
	    paint.setColor(mSeekLineColor);
	    canvas.drawLine(x, y, x+width, y, paint);
	}
	
	private void drawSeekLineText(Canvas canvas, int x, int y, int width, String str) {
	    TextPaint paint = new TextPaint();
	    paint.setColor(mSeekLineTextColor);
	    paint.setTextSize(mSeekLineTextSize);
	    paint.setTextAlign(Align.LEFT);
	    canvas.drawText(str, x, y+mSeekLineTextSize/2, paint);
	}
	
	private boolean isTouchDownAtTriangle(float x, float y) {
	    float length = mSeekTriangleLength;
	    float originX = 0;
	    float originY = getHeight()/2 - length*1.5f;
	    if (x>=0 && x<=length*2 && y>=originY && y<=(originY+length*3)) {
	        return true;
	    }
	    return false;
	}
	
	public void seekLrc(int position, boolean cb) {
	    if(mLrcRows == null || position < 0 || position > mLrcRows.size()) {
	        return;
	    }
		LrcRow lrcRow = mLrcRows.get(position);
		mHignlightRow = position;
		if (mDisplayMode != DISPLAY_MODE_SEEK) {
		    mSeekedRow = position;
		}
		invalidate();
		if(mLrcViewListener != null && cb){
			mLrcViewListener.onLrcSeeked(position, lrcRow);
		}
	}

	private float mLastMotionY;
	private PointF mPointerOneLastMotion = new PointF();
	private PointF mPointerTwoLastMotion = new PointF();
	private boolean mIsFirstMove = false; // whether is first move , some events can't not detected in touch down,
										  // such as two pointer touch, so it's good place to detect it in first move

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(mLrcRows == null || mLrcRows.size() == 0){
			return super.onTouchEvent(event);
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG,"down,mLastMotionY:"+mLastMotionY);
			mLastMotionY = event.getY();
			mIsFirstMove = true;
			Log.d(TAG, "onTouchEvent:ACTION_DOWN x " + event.getX() + " y " + event.getY());
			if (isTouchDownAtTriangle(event.getX(), event.getY())) {
			    mDisplayMode = DISPLAY_MODE_NORMAL;
			    seekLrc(mSeekedRow, true);
			}
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:

			if(event.getPointerCount() == 2){
				Log.d(TAG, "two move");
//				doScale(event); // disable
				return true;
			}
			Log.d(TAG, "one move");
			// single pointer mode ,seek
			if(mDisplayMode == DISPLAY_MODE_SCALE){
				 //if scaling but pointer become not two ,do nothing.
				return true;
			}

			doSeek(event);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// 保持一段时间为Seek模式，超时后切换成Normal模式。此间若move，则重新计时。
			mHandler.removeCallbacks(mRunnableSwitchToNormal);
			mHandler.postDelayed(mRunnableSwitchToNormal, LRCVIEW_SEEK_MODEL_DELAY);
			invalidate();
			break;
		}
		return true;
	}
	private void doScale(MotionEvent event) {
		if(mDisplayMode == DISPLAY_MODE_SEEK){
			// if Seeking but pointer become two, become to scale mode
			mDisplayMode = DISPLAY_MODE_SCALE;
			Log.d(TAG, "two move but teaking ...change mode");
			return;
		}
		// two pointer mode , scale font
		if(mIsFirstMove){
			mDisplayMode = DISPLAY_MODE_SCALE;
			invalidate();
			mIsFirstMove = false;
			setTwoPointerLocation(event);
		}
		int scaleSize = getScale(event);
		Log.d(TAG,"scaleSize:" + scaleSize);
		if(scaleSize != 0){
			setNewFontSize(scaleSize);
			invalidate();
		}
		setTwoPointerLocation(event);
	}

	private void doSeek(MotionEvent event) {
		float y = event.getY();
		float offsetY = y - mLastMotionY; // touch offset.
		if(Math.abs(offsetY) < mMinSeekFiredOffset){
			// move to short ,do not fire seek action
			return;
		}
		mDisplayMode = DISPLAY_MODE_SEEK;
		int rowOffset = Math.abs((int) offsetY / mLrcFontSize); // highlight row offset.
		if(offsetY < 0){
			// finger move up
			mSeekedRow = mSeekedRow + rowOffset;
		}else if(offsetY > 0){
			// finger move down
			mSeekedRow = mSeekedRow - rowOffset;
		}
		mSeekedRow = Math.max(0, mSeekedRow);
		mSeekedRow = Math.min(mSeekedRow, mLrcRows.size() - 1);

		if(rowOffset > 0){
			mLastMotionY = y;
			invalidate();
		}
	}

	private void setTwoPointerLocation(MotionEvent event) {
		mPointerOneLastMotion.x = event.getX(0);
		mPointerOneLastMotion.y = event.getY(0);
		mPointerTwoLastMotion.x = event.getX(1);
		mPointerTwoLastMotion.y = event.getY(1);
	}

	private void setNewFontSize(int scaleSize){
		mLrcFontSize += scaleSize;
		mSeekLineTextSize += scaleSize;
		mLrcFontSize = Math.max(mLrcFontSize, mMinLrcFontSize);
		mLrcFontSize = Math.min(mLrcFontSize, mMaxLrcFontSize);
		mSeekLineTextSize = Math.max(mSeekLineTextSize, mMinSeekLineTextSize);
		mSeekLineTextSize = Math.min(mSeekLineTextSize, mMaxSeekLineTextSize);
	}

	// get font scale offset
	private int getScale(MotionEvent event){
		Log.d(TAG,"scaleSize getScale");
		float x0 = event.getX(0);
		float y0 = event.getY(0);
		float x1 = event.getX(1);
		float y1 = event.getY(1);
		float maxOffset =  0; // max offset between x or y axis,used to decide scale size

		boolean zoomin = false;

		float oldXOffset = Math.abs(mPointerOneLastMotion.x - mPointerTwoLastMotion.x);
		float newXoffset = Math.abs(x1 - x0);

		float oldYOffset = Math.abs(mPointerOneLastMotion.y - mPointerTwoLastMotion.y);
		float newYoffset = Math.abs(y1 - y0);

		maxOffset = Math.max(Math.abs(newXoffset - oldXOffset), Math.abs(newYoffset - oldYOffset));
		if(maxOffset == Math.abs(newXoffset - oldXOffset)){
			zoomin = newXoffset > oldXOffset ? true : false;
		}else{
			zoomin = newYoffset > oldYOffset ? true : false;
		}

		Log.d(TAG,"scaleSize maxOffset:" + maxOffset);

		if(zoomin)
			return (int)(maxOffset / 10);
		else
			return -(int)(maxOffset / 10);
	}

    public void setLrc(List<LrcRow> lrcRows) {
        mLrcRows = lrcRows;
        invalidate();
    }

    public void seekLrcToTime(long time) {
        if(mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }

        Log.d(TAG, "seekLrcToTime:" + time);
        // find row
        for(int i = 0; i < mLrcRows.size(); i++) {
            LrcRow current = mLrcRows.get(i);
            LrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);

            if((time >= current.time && next != null && time < next.time)
                    || (time > current.time && next == null)) {
                seekLrc(i, false);
                return;
            }
        }
    }
}
