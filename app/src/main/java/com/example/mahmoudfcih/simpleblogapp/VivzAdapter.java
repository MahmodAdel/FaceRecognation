package com.example.mahmoudfcih.simpleblogapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by mahmoud on 2/16/2017.
 */

public class VivzAdapter extends RecyclerView.Adapter<VivzAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private Context context;
    private ClickListener clicklistener;

    List<Information> data= Collections.emptyList();

    public VivzAdapter(Context context,List<Information> data)
    {
        this.context=context;
        inflater =LayoutInflater.from(context);
        this.data=data;


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.custom_raw,parent,false);  //1
        MyViewHolder holder=new MyViewHolder(view); //2



        return holder; //4
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Information current=data.get(position);
        holder.title.setText(current.title);
        holder.icon.setImageResource(current.iconId);
        //  Log.i("VIVZ","clicked:"+position);

    }
    public void setClickListener(ClickListener clicklistener)
    {
        this.clicklistener=clicklistener;

    }
    public void delete(int postion){
        data.remove(postion);
        notifyItemRemoved(postion);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        ImageView icon;


        public MyViewHolder(View itemView) { //3
            super(itemView);
            itemView.setOnClickListener(this);
            title= (TextView) itemView.findViewById(R.id.listText);
            icon= (ImageView) itemView.findViewById(R.id.listIcon);



        }

        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            //   context.startActivity(new Intent(context,SubActivity.class));
            if(clicklistener !=null)
            {
                clicklistener.itemClick(view,position);
            }
            //  Toast.makeText(view.getContext(), "position = " + position, Toast.LENGTH_SHORT).show();
        }
    }
    public interface ClickListener{
        public void itemClick(View view ,int position);
    }
}
