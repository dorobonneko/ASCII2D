package com.moe.ascii2d;

import android.app.*;
import android.os.*;
import android.view.View;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapRegionDecoder;
import java.io.IOException;
import android.graphics.BitmapFactory;
import com.moe.ascii2d.utils.BitmapUtils;
import android.util.TypedValue;
import android.graphics.Rect;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.widget.CheckBox;
import com.moe.ascii2d.net.QueryHash;
import android.widget.Toolbar;
import android.widget.ListView;
import android.view.WindowInsets;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.Connection;
import java.util.List;
import org.jsoup.nodes.Element;
import com.moe.ascii2d.empty.ResultElement;
import com.moe.ascii2d.adapter.ResultAdapter;
import java.util.ArrayList;
import android.graphics.drawable.BitmapDrawable;
import android.widget.AdapterView;
import android.widget.Adapter;
import android.net.Uri;
import com.moe.ascii2d.utils.Path;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements View.OnClickListener,QueryHash.OnHashReceivedListener,View.OnApplyWindowInsetsListener
{
	private Dialog addDialog;
	private String path;
	//private ParcelFileDescriptor mParcelFileDescriptor;
	private BitmapRegionDecoder mBitmapRegionDecoder;
	private BitmapFactory.Options options;
	private String mode;
	private QueryHash mQueryId;
	private View progressBar,add;
	private ActionBar mActionBar;
	private ListView listview;
	private String hash;
	private List<ResultElement> list;
	private ResultAdapter mResultAdapter;
	private int width,height;
	private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		add = findViewById(R.id.add);
		add.setOnClickListener(this);
		progressBar = findViewById(R.id.progressBar);
		setActionBar((Toolbar)findViewById(R.id.toolbar));
		mActionBar = getActionBar();
		listview = findViewById(R.id.listview);
		listview.setAdapter(mResultAdapter=new ResultAdapter(list=new ArrayList<>()));
		listview.setDivider(new BitmapDrawable());
		listview.setDividerHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,16,getResources().getDisplayMetrics()));
		View content=findViewById(R.id.content);
		content.setFitsSystemWindows(true);
		content.setOnApplyWindowInsetsListener(this);
		if (Intent.ACTION_VIEW.equals(getIntent().getAction()))
		{
			add.setVisibility(View.INVISIBLE);
			progressBar.setVisibility(View.VISIBLE);
			mActionBar.setSubtitle(getIntent().getData().getLastPathSegment());
			loadPage(getIntent().getDataString());
			
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case R.id.history:{
				Intent intent=new Intent(Intent.ACTION_VIEW);
				intent.setClass(getApplicationContext(),MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setData(Uri.parse(Ascii2D.PROPERTY.concat("://").concat(Ascii2D.HOST).concat("/recently")));
				try{startActivity(intent);}catch(Exception e){}
				
				}break;
			case R.id.hot:{
					Intent intent=new Intent(Intent.ACTION_VIEW);
					intent.setClass(getApplicationContext(),MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setData(Uri.parse(Ascii2D.PROPERTY.concat("://").concat(Ascii2D.HOST).concat("/ranking/daily")));
					try{startActivity(intent);}catch(Exception e){}
					
			}
				break;
			case R.id.open_in_browser:
				if(url!=null){
					Intent intent=new Intent(Intent.ACTION_VIEW);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setData(Uri.parse(url));
					try{startActivity(intent);}catch(Exception e){}
					
				}
				break;
		}
		return true;
	}

	@Override
	public void onClick(View p1)
	{
		switch (p1.getId())
		{
			case R.id.add:
				if (mBitmapRegionDecoder != null)
					mBitmapRegionDecoder.recycle();
				mBitmapRegionDecoder = null;
				Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, 322);
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 322 && resultCode == RESULT_OK)
		{
			if (addDialog == null)
				addDialog = new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string.chooserImage).setView(R.layout.add).setNegativeButton(R.string.colorSearch, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							mode = "color";
							query(((CheckBox)addDialog.findViewById(R.id.scaleImage)).isChecked());
						}
					}).setPositiveButton(R.string.bovwSearch, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							mode = "bovw";
							query(((CheckBox)addDialog.findViewById(R.id.scaleImage)).isChecked());
						}
					}).setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							if (mBitmapRegionDecoder != null)
								mBitmapRegionDecoder.recycle();
							mBitmapRegionDecoder = null;
						}
					}).create();
			addDialog.show();
			try
			{
				switch(data.getData().getScheme()){
					case "file":
						path=data.getData().getPath();
						break;
					case "content":
						path=Path.getPath(getApplicationContext(),data.getData());
						break;
				}
				if(path==null)return;
				if (options == null)
					options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				options.inSampleSize = 1;
				BitmapFactory.decodeFile(path, options);
				BitmapRegionDecoder brd=BitmapRegionDecoder.newInstance(path, false);
				float sampleSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
				options.inSampleSize = BitmapUtils.calculateInSampleSize(options.outWidth, options.outHeight, sampleSize, sampleSize * 2);
				options.inJustDecodeBounds = false;
				width=options.outWidth;
				height=options.outHeight;
				Bitmap bitamp=brd.decodeRegion(new Rect(0, 0, options.outWidth, options.outHeight), options);
				((ImageView)addDialog.findViewById(R.id.preview)).setImageBitmap(bitamp);
				
				mBitmapRegionDecoder = brd;
			}
			catch (IOException e)
			{}
		}
	}
	private void query(boolean scale)
	{
		progressBar.setVisibility(View.VISIBLE);
		add.setEnabled(false);
		if (mQueryId != null)
			mQueryId.cancel();
		if (scale)
			mQueryId = new QueryHash(mBitmapRegionDecoder.decodeRegion(new Rect(0, 0, width, height), options));
		else
			mQueryId = new QueryHash(path);
		mQueryId.exec(this);
	}

	@Override
	public void onHashReceived(final String id)
	{
		mQueryId = null;
		hash=id;
		//加载网页数据
		runOnUiThread(new Runnable(){

				@Override
				public void run()
				{
					mActionBar.setSubtitle(id);
					if (id == null)
					{
						progressBar.setVisibility(View.INVISIBLE);
						add.setEnabled(true);
						Toast.makeText(getApplicationContext(), R.string.loadFail, Toast.LENGTH_SHORT).show();
					}
				}
			});
		if(hash!=null)
			loadPage(Ascii2D.PROPERTY.concat("://").concat(Ascii2D.HOST).concat("/search/").concat(mode).concat("/").concat(hash));
	}

	@Override
	public WindowInsets onApplyWindowInsets(View p1, WindowInsets p2)
	{
		p1.setPadding(p2.getSystemWindowInsetLeft(), p2.getSystemWindowInsetTop(), p2.getSystemWindowInsetRight(), p2.getSystemWindowInsetBottom());
		//listview.setPadding(0,0,0,p2.getSystemWindowInsetBottom());
		//listview.setClipToPadding(false);
		return p2;
	}

	private void loadPage(final String url)
	{
		this.url=url;
		new Thread(){
			public void run(){
				try
				{
					Connection conn=Jsoup.connect(url);
					Document doc=conn.get();
					Elements elements=doc.getElementsByClass("item-box");
					Elements headers=doc.getElementsByClass("item-header");
					final List<ResultElement> tempList=new ArrayList<>(elements.size());
					for(int i=0;i<elements.size();i++){
						Element element=elements.get(i);
						ResultElement result=new ResultElement();
						result.hash=element.getElementsByClass("hash").get(0).text();
						result.previewUrl=element.child(0).child(0).attr("abs:src");
						Elements info=element.getElementsByClass("text-muted");
						if(!info.isEmpty()){
						String[] infos=info.get(0).text().split(" ");
						result.pixel=infos[0];
						result.format=infos[1];
						result.size=infos[2];
						}
						Elements sourceInfo=element.getElementsByAttributeValue("rel","noopener");
						result.noopener=new ArrayList<>(sourceInfo.size());
						if(!headers.isEmpty()){
							Elements  noopeners=headers.get(i).getElementsByAttributeValue("rel","noopener");
							for(int n=0;n<noopeners.size();n++){
								Element noopener=noopeners.get(n);
							result.noopener.add(new String[]{noopener.text(),noopener.attr("abs:href")});
							}
						}
						for(int n=0;n<sourceInfo.size();n++){
							Element noopener=sourceInfo.get(n);
							if(noopener.hasText()){
								result.noopener.add(new String[]{noopener.text(),noopener.attr("abs:href")});
							}else{
								result.noopener.add(new String[]{noopener.child(0).attr("alt"),noopener.attr("abs:href")});
							}
						}
						Elements site=element.getElementsByClass("to-link-icon");
						if(site.size()>0){
							result.siteName=site.attr("alt");
						}else if(info.size()>1){
							if(info.get(1).children().isEmpty())
							result.siteName=info.get(1).text();
						}
						Elements h6=element.getElementsByTag("h6");
						if(!h6.isEmpty())
						result.title=h6.get(0).ownText();
						tempList.add(result);
					}
					runOnUiThread(new Runnable(){

							@Override
							public void run()
							{
								list.clear();
								list.addAll(tempList);
								tempList.clear();
								mResultAdapter.notifyDataSetChanged();
								progressBar.setVisibility(View.INVISIBLE);
								add.setEnabled(true);
							}
						});
				}
				catch (IOException e)
				{
					runOnUiThread(new Runnable(){

							@Override
							public void run()
							{
								list.clear();
								mResultAdapter.notifyDataSetChanged();
								progressBar.setVisibility(View.INVISIBLE);
								add.setEnabled(true);
								Toast.makeText(getApplicationContext(),R.string.loadFail,Toast.LENGTH_SHORT).show();
							}
						});
				}
			}
		}.start();
	}

}
