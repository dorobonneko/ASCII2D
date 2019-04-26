package com.moe.ascii2d.adapter;
import android.view.*;
import android.widget.*;

import android.text.Html;
import com.moe.ascii2d.R;
import com.moe.ascii2d.empty.ResultElement;
import java.util.List;
import android.content.Intent;
import android.net.Uri;
import com.moe.ascii2d.MainActivity;
import com.moe.ascii2d.Ascii2D;
import com.moe.tinyimage.TinyImage;

public class ResultAdapter extends BaseAdapter
{
	private List<ResultElement> list;
	public ResultAdapter(List<ResultElement> list){
		this.list=list;
	}
	@Override
	public int getCount()
	{
		return list.size();
	}

	@Override
	public Object getItem(int p1)
	{
		return list.get(p1);
	}

	@Override
	public long getItemId(int p1)
	{
		return p1;
	}

	@Override
	public View getView(int p1, View p2, ViewGroup p3)
	{
		if(p2==null){
			p2=LayoutInflater.from(p3.getContext()).inflate(R.layout.result_item_view,p3,false);
		}
		ResultElement result=list.get(p1);
		ViewHolder vh=(ResultAdapter.ViewHolder) p2.getTag();
		if(vh==null)
			p2.setTag(vh=new ViewHolder(p2));
		vh.position=p1;
		TinyImage.get(p2.getContext()).load(result.previewUrl,vh.preview).commit();
		vh.summary.setText(Html.fromHtml(String.format("%1$s %2$s %3$s <font color=\"red\">%4$s</font> %5$s",result.pixel,result.format,result.size,result.siteName==null?"":result.siteName,result.title==null?"":result.title)));
		vh.title.setText(result.hash);
		return p2;
	}

	@Override
	public int getItemViewType(int position)
	{
		return position;
	}

	@Override
	public int getViewTypeCount()
	{
		return list.isEmpty()?1:list.size();
	}
	
	class ViewHolder implements View.OnClickListener,PopupMenu.OnMenuItemClickListener{
		TextView title,summary;
		ImageView preview;
		public int position;
		View itemview;
		ViewHolder(View v){
			itemview=v;
			preview=v.findViewById(R.id.preview);
			title=v.findViewById(R.id.title);
			summary=v.findViewById(R.id.summary);
			v.findViewById(R.id.menu).setOnClickListener(this);
		}

		@Override
		public void onClick(View p1)
		{
			switch(p1.getId()){
				case R.id.menu:
					PopupMenu pm=(PopupMenu) p1.getTag();
					if(pm==null){
						p1.setTag(pm=new PopupMenu(p1.getContext(),p1));
						pm.setOnMenuItemClickListener(this);
						p1.setOnTouchListener(pm.getDragToOpenListener());
					}
					ResultElement result=list.get(position);
					pm.getMenu().clear();
					for(int i=0;i<result.noopener.size();i++){
						pm.getMenu().add(0,i,0,result.noopener.get(i)[0]);
					}
					pm.inflate(R.menu.menu_popup);
					pm.show();
					break;
			}
		}

		@Override
		public boolean onMenuItemClick(MenuItem p1)
		{
			switch(p1.getItemId()){
				case R.id.colorSearch:{
					Intent intent=new Intent(Intent.ACTION_VIEW);
					intent.setClass(itemview.getContext(),MainActivity.class);
					intent.setData(Uri.parse(Ascii2D.PROPERTY.concat("://").concat(Ascii2D.HOST).concat("/search/").concat("color/").concat(list.get(position).hash)));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					try{itemview.getContext().startActivity(intent);}catch(Exception e){}
					
					}break;
				case R.id.bovwSearch:{
					Intent intent=new Intent(Intent.ACTION_VIEW);
					intent.setClass(itemview.getContext(),MainActivity.class);
					intent.setData(Uri.parse(Ascii2D.PROPERTY.concat("://").concat(Ascii2D.HOST).concat("/search/").concat("bovw/").concat(list.get(position).hash)));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					try{itemview.getContext().startActivity(intent);}catch(Exception e){}
					
					}break;
				default:{
				Intent intent=new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(list.get(position).noopener.get(p1.getItemId())[1]));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try{itemview.getContext().startActivity(intent);}catch(Exception e){}
				}break;
			}
			return true;
		}


		
	}
}
