package com.regar007.shapesinopengles20;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.regar007.shapesinopengles20.GLSurface.ShapeGLSurfaceView;
import com.regar007.shapesinopengles20.GLSurface.ShapeRenderer;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShapeActivity extends Activity {
	/** Used for debug logs. */
	private static final String TAG = "ShapeActivity";

	/** Hold a reference to our GLSurfaceView **/
	private ShapeGLSurfaceView aGLSurfaceView;
	private ShapeRenderer aRenderer;
	public static int shape;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(shape == 0) {
			setTitle(R.string.shape_one);
		}
		else if(shape == 1){
			setTitle(R.string.shape_two);
		}
        else if(shape == 2){
            setTitle(R.string.shape_three);
        }
		else if(shape == 3){
			setTitle(R.string.shape_four);
		}
		else if(shape == 4){
			setTitle(R.string.shape_five);
		}
		else if(shape == 5){
			setTitle(R.string.shape_six);
		}
		else if(shape == 6){
			setTitle(R.string.shape_seven);
		}

		setContentView(R.layout.shapes);

		aGLSurfaceView = (ShapeGLSurfaceView) findViewById(R.id.gl_surface_view);

		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) {
			// Request an OpenGL ES 2.0 compatible context.
			aGLSurfaceView.setEGLContextClientVersion(2);

			final DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			// Set the renderer to our demo renderer, defined below.
			aRenderer = new ShapeRenderer(this, aGLSurfaceView, shape);
			aGLSurfaceView.setRenderer(aRenderer, displayMetrics.density, shape);
		} else {
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}
	}

	@Override
	protected void onResume() {
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
		super.onResume();
		aGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		super.onPause();
		aGLSurfaceView.onPause();
	}

}