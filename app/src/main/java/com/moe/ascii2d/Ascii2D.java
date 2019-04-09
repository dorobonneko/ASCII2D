package com.moe.ascii2d;

public class Ascii2D
{
	public final static String HOST="www.ascii2d.net";
	public final static String PROPERTY="http";
	public final static String SPLIT="moe";
	public final static byte[] START=String.format("--%1$s\r\nContent-Disposition: form-data; name=\"utf8\"\r\n\r\n✓\r\n--%1$s\r\nContent-Disposition: form-data; name=\"authenticity_token\"\r\n\r\nHnaOGVvLSB5DVN92Z3rJcPckQq0vih1yNHCm4eD2aH2hfk+5xE/W9zLd+S/m1sI0cOgCzFNHAb0iJHhponL0rQ==\r\n--%1$s\r\nContent-Disposition: form-data; name=\"file\"; filename=\"file.jpg\"\r\nContent-Type: image/jpeg\r\n\r\n",SPLIT).getBytes();
	public final static byte[] END=String.format("\r\n--%1$s\r\nContent-Disposition: form-data; name=\"search\"\r\n\r\nr\n--%1$s--\r\n",SPLIT).getBytes();
}
