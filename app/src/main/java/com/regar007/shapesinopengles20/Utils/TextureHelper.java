package com.regar007.shapesinopengles20.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
/**
 * Created by regar007.
 *
 * <p>
 *     This is created to read a texture from an image on file system.
 * </p>
 */

public class TextureHelper
{
    /**
     * Use this function to read texture from image on system.
     * @param context
     * @param resourceId
     * @return handle to texture
     */
	public static int loadTexture(final Context context, final int resourceId, boolean allowWrap)
	{
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0)
		{
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;	// No pre-scaling

			// Read in the resource
			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			if(allowWrap) {
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
						GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
						GLES20.GL_CLAMP_TO_EDGE);
			}

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();
		}

		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	public static int loadTextureFromBitmap(final Context context, Bitmap bm)
	{
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0)
		{
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;	// No pre-scaling

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
					GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
					GLES20.GL_CLAMP_TO_EDGE);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bm, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bm.recycle();
		}

		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}
}
