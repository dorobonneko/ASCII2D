package com.moe.ascii2d.net;

public interface Query
{
	void cancel();
	void exec(OnHashReceivedListener l);
	public interface OnHashReceivedListener
	{
		void onHashReceived(String hash);
	}
}
