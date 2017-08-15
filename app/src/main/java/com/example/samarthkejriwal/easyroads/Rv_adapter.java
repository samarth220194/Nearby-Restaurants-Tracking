package com.example.samarthkejriwal.easyroads;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.samarthkejriwal.easyroads.Modal.PlacesDetails_Modal;
import com.example.samarthkejriwal.easyroads.Rv_adapter.MyViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.samarthkejriwal.easyroads.R.id.distance;

/**
 * Created by samarthkejriwal on 10/08/17.
 */

public class Rv_adapter extends RecyclerView.Adapter<MyViewHolder> {

    private ArrayList<PlacesDetails_Modal> storeModels;
    private Context context;
    private String current_address;

    private static final int TYPE_HEAD=0;
    private static final int TYPE_LIST=1;


    public Rv_adapter(Context context ,ArrayList<PlacesDetails_Modal> storeModels,String current_address)
    {

        this.context = context;
        this.storeModels = storeModels;
        this.current_address = current_address;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType==TYPE_LIST) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_listitem, parent, false);

            return new MyViewHolder(itemView,viewType);
        }
        else if(viewType==TYPE_HEAD)
        {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_header, parent, false);

            return new MyViewHolder(itemView,viewType);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {

        if(position==0)
        {
            return TYPE_HEAD;
        }
        else{
            return TYPE_LIST;
        }

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
            if(holder.view_type == TYPE_LIST)
            {
                holder.res_name.setText(storeModels.get(holder.getAdapterPosition()-1).name);

                Picasso.with(context).load(storeModels.get(holder.getAdapterPosition() - 1).photourl)
                        .placeholder(R.drawable.placeholder).resize(100, 100).into(holder.res_image);


                holder.res_address.setText(storeModels.get(holder.getAdapterPosition() - 1).address);

                if(storeModels.get(holder.getAdapterPosition() - 1).phone_no == null)
                {
                    holder.res_phone.setText("N/A");
                }
                else  holder.res_phone.setText(storeModels.get(holder.getAdapterPosition() - 1).phone_no);

                holder.res_rating.setText(String.valueOf(storeModels.get(holder.getAdapterPosition() - 1).rating));

                holder.res_distance.setText(storeModels.get(holder.getAdapterPosition() - 1).distance);

                Log.i("details on adapter", storeModels.get(holder.getAdapterPosition()-1).name + "  " +
                       storeModels.get(holder.getAdapterPosition()-1).address +
                        "  " +  storeModels.get(holder.getAdapterPosition() - 1).distance);
            }

            else if (holder.view_type == TYPE_HEAD)
            {
                if(current_address == null)
                {
                    holder.current_location.setText("Unable to Detect Current Location");
                }
                else {
                    holder.current_location.setText(current_address);
                }
            }
    }

    @Override
    public int getItemCount() {

        return  storeModels.size()+1;

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView res_name;
        TextView res_rating;
        TextView res_address;
        TextView res_phone;
        TextView res_distance;
        TextView current_location;
        ImageView res_image,location_image;

        int view_type;

        public MyViewHolder(final View itemView, final int viewType) {
            super(itemView);

            if(viewType == TYPE_LIST) {

                view_type=1;
                this.res_name = (TextView) itemView.findViewById(R.id.name);
                this.res_rating = (TextView) itemView.findViewById(R.id.rating);
                this.res_address = (TextView) itemView.findViewById(R.id.address);
                this.res_phone = (TextView) itemView.findViewById(R.id.phone);
                this.res_distance = (TextView) itemView.findViewById(distance);
                this.res_image = (ImageView) itemView.findViewById(R.id.res_image);
            }
          else  if(viewType == TYPE_HEAD){
                view_type = 0;
                this.current_location = (TextView) itemView.findViewById(R.id.location_tv);
                this.location_image = (ImageView) itemView.findViewById(R.id.current_location);
                location_image.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("button","clicked");
                        Intent intent = new Intent(view.getContext(),MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        view.getContext().startActivity(intent);

                    }
                });

            }

        }


    }
}
