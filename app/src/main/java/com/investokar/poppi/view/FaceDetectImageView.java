package com.investokar.poppi.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.investokar.poppi.R;

public class FaceDetectImageView extends AppCompatImageView {


	public FaceDetectImageView(Context context) {
		super(context);
		sharedConstructing(context);
	}

	public FaceDetectImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedConstructing(context);
	}

	private int imageWidth, imageHeight;
	private int numberOfFace = 1;
	private FaceDetector myFaceDetect;
	private FaceDetector.Face[] myFace;
	float myEyesDistance;
	int numberOfFaceDetected;
	Bitmap myBitmap;

	public void sharedConstructing(Context context) {
			BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
			BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
			myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_default_photo, BitmapFactoryOptionsbfo);
			imageWidth = myBitmap.getWidth();
			imageHeight = myBitmap.getHeight();
			myFace = new FaceDetector.Face[numberOfFace];
			myFaceDetect = new FaceDetector(imageWidth, imageHeight, numberOfFace);
			numberOfFaceDetected = myFaceDetect.findFaces(myBitmap, myFace);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(myBitmap, 0, 0, null);
		Paint myPaint = new Paint();
		myPaint.setColor(Color.GREEN);
		myPaint.setStyle(Paint.Style.STROKE);
		myPaint.setStrokeWidth(3);
		for (int i = 0; i < numberOfFaceDetected; i++) {
			FaceDetector.Face face = myFace[i];
			PointF myMidPoint = new PointF();
			face.getMidPoint(myMidPoint);
			myEyesDistance = face.eyesDistance();
			canvas.drawRect((int) (myMidPoint.x - myEyesDistance * 2),
					(int) (myMidPoint.y - myEyesDistance * 2),
					(int) (myMidPoint.x + myEyesDistance * 2),
					(int) (myMidPoint.y + myEyesDistance * 2), myPaint);
		}
	}

}
