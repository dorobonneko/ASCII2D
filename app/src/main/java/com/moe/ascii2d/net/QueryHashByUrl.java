package com.moe.ascii2d.net;

import java.io.*;
import java.net.*;

import android.net.Uri;
import com.moe.ascii2d.Ascii2D;

public class QueryHashByUrl implements Query
{
	private OnHashReceivedListener mOnHashReceivedListener;
	private HttpURLConnection huc=null;
	private OutputStream output=null;
	private String url;
	public QueryHashByUrl(String url){
		this.url=url;
	}
	public void exec(OnHashReceivedListener l)
	{
		mOnHashReceivedListener = l;
		new Thread(){
			public void run()
			{

				try
				{
					StringBuilder sb=new StringBuilder();
					sb.append("utf8=✓&authenticity_token=CWUnK9YPgpmtiAEFiS64wSkcAX1RL4tS+TkFzTeK0/i9FC0UaoXMzk8mrTsWDQAREVwNndvYcRDFbkeNp1uPiQ==&uri=");
					sb.append(url);
					byte[] data=sb.toString().getBytes();
					huc = (HttpURLConnection) new URL(Ascii2D.PROPERTY.concat("://").concat(Ascii2D.HOST).concat("/search/uri")).openConnection();
					huc.setFollowRedirects(false);
					huc.setInstanceFollowRedirects(false);
					huc.setUseCaches(false);
					huc.setDoOutput(true);
					huc.setDoInput(false);
					//huc.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", Ascii2D.SPLIT));
					huc.setRequestProperty("Content-Length", String.valueOf(data.length));
					huc.setRequestProperty("Connection", "close");
					output = huc.getOutputStream();
					//写入数据
					output.write(data);
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
			if (output != null)
				output.close();
		}
		catch (IOException e)
		{}
		if (huc != null)huc.disconnect();
	}
}
