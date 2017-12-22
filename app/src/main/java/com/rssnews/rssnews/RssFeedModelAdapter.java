package com.rssnews.rssnews;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by valentin on 14.12.2017.
 */

public class RssFeedModelAdapter extends RecyclerView.Adapter<RssFeedModelAdapter.MyViewHolder> {

    private List<RssFeedModel> feedList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleText , gelinkTextnre;
        public WebView descriptionText;

        public MyViewHolder(View view) {
            super(view);


            titleText = (TextView) view.findViewById(R.id.titleText);
            titleText.setTextSize(20);
            descriptionText = (WebView) view.findViewById(R.id.descriptionText);
            descriptionText.getSettings().setJavaScriptEnabled(true);
            gelinkTextnre = (TextView) view.findViewById(R.id.linkText);
            gelinkTextnre.setTextSize(16);
            gelinkTextnre.setMovementMethod(LinkMovementMethod.getInstance());

        }
    }


    public RssFeedModelAdapter(List<RssFeedModel> feedList) {
        this.feedList = feedList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rss_feed, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(RssFeedModelAdapter.MyViewHolder holder, int position) {
        RssFeedModel rssFeedModel = feedList.get(position);
        holder.titleText.setText(rssFeedModel.title);
        holder.descriptionText.loadDataWithBaseURL("", rssFeedModel.description, "text/html", "UTF-8", "");
        holder.gelinkTextnre.setText(Html.fromHtml("<a href=\""+rssFeedModel.link+"\">"+rssFeedModel.link+"</a>"));
        holder.gelinkTextnre.setLinkTextColor(R.color.ambiantLink);
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }
}
