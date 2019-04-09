package com.moe.ascii2d.net;
import android.graphics.Bitmap;
import java.io.FileDescriptor;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import com.moe.ascii2d.Ascii2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.net.Uri;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.File;

public class QueryHash
{
	private OnHashReceivedListener mOnHashReceivedListener;
	private Bitmap bitmap;
	private String path;
	private HttpURLConnection huc=null;
	private OutputStream output=null;
	private InputStream input=null;
	private ByteArrayOutputStream baos;
	public QueryHash(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}
	public QueryHash(String fd)
	{
		this.path = fd;
	}
	public void exec(OnHashReceivedListener l)
	{
		mOnHashReceivedListener = l;
		new Thread(){
			public void run()
			{

				try
				{
					baos = new ByteArrayOutputStream();
					File file=null;
					
					if (bitmap!=null)
					{
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					}else{
						file=new File(path);
					}

					huc = (HttpURLConnection) new URL(Ascii2D.PROPERTY.concat("://").concat(Ascii2D.HOST).concat("/search/file")).openConnection();
					huc.setFollowRedirects(false);
					huc.setInstanceFollowRedirects(false);
					huc.setUseCaches(false);
					huc.setDoOutput(true);
					huc.setDoInput(false);
					huc.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", Ascii2D.SPLIT));
					huc.setRequestProperty("Content-Length", String.valueOf(Ascii2D.START.length + Ascii2D.END.length+(bitmap!=null? baos.size():file.length())));
					huc.setRequestProperty("Connection", "close");
					output = huc.getOutputStream();
					//写入数据
					output.write(Ascii2D.START);
					if(bitmap!=null)
					output.write(baos.toByteArray());
					else{
						input=new FileInputStream(file);
						byte[] buffer=new byte[2048];
						int len=-1;
						while ((len = input.read(buffer)) != -1)
						{
							output.write(buffer, 0, len);
						}
					}
					output.write(Ascii2D.END);
					output.flush();
					//output.close();
					if (huc.getResponseCode() == 302)
					{
						if (mOnHashReceivedListener != null)
							mOnHashReceivedListener.onHashReceived(Uri.parse(huc.getHeaderField("Location")).getLastPathSegment());
					}else{
						throw new IOException();
					}
				}
				catch (IOException e)
				{
					if(mOnHashReceivedListener!=null)
						mOnHashReceivedListener.onHashReceived(null);
				}
				finally
				{
					cancel();
				}
			}
		}.start();
	}
	public void cancel()
	{
		mOnHashReceivedListener = null;
		try
		{
			if (baos != null)baos.close();
		}
		catch (IOException e)
		{}
		try
		{
			if (input != null)
				input.close();
		}
		catch (IOException e)
		{}
		try
		{
			if (output != null)
				output.close();
		}
		catch (IOException e)
		{}
		if (huc != null)huc.disconnect();
	}
	public interface OnHashReceivedListener
	{
		void onHashReceived(String hash);
	}
}
