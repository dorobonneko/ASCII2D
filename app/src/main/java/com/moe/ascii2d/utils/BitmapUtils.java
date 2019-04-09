package com.moe.ascii2d.utils;

public class BitmapUtils
{
	public static int calculateInSampleSize(int width, int height, float scale)
	{
        final float reqHeight = height * scale;
        final float reqWidth = width * scale;
        return calculateInSampleSize(width,height,reqWidth,reqHeight);
    }
	public static int calculateInSampleSize(int width, int height, float reqWidth,float reqHeight)
	{
         int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth)
		{
            final int halfHeight = height;
            final int halfWidth = width;
            while ((halfHeight / inSampleSize) > reqHeight
				   && (halfWidth / inSampleSize) > reqWidth)
			{
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
