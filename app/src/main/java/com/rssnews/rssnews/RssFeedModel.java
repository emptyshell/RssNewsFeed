package com.rssnews.rssnews;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class RssFeedModel {

    public int id=0;
    public String title;
    public String link;
    public String description;

    public RssFeedModel(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }
    public  RssFeedModel() {
        this.title = "";
        this.link= "";
        this.description ="";
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleText, descriptionText, gelinkTextnre;

        public MyViewHolder(View view) {
            super(view);
            titleText = (TextView) view.findViewById(R.id.titleText);
            descriptionText = (TextView) view.findViewById(R.id.descriptionText);
            gelinkTextnre = (TextView) view.findViewById(R.id.linkText);
        }
    }
}
