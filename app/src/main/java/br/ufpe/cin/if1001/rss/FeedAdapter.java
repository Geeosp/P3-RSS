package br.ufpe.cin.if1001.rss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.ufpe.cin.if1001.rss.FeedItem;
import br.ufpe.cin.if1001.rss.FeedItemHolder;
import br.ufpe.cin.if1001.rss.R;

class FeedAdapter extends BaseAdapter {
    Context context;

    List<FeedItem> feedItens;

    public void setFeedList(List<FeedItem> list) {
        this.feedItens = list;
    }

    public FeedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return feedItens.size();
    }

    @Override
    public FeedItem getItem(int i) {
        return feedItens.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        FeedItemHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_feed, viewGroup, false);
            holder = new FeedItemHolder();
            holder.tv_description = (TextView) view.findViewById(R.id.tv_content);
            holder.tv_title = (TextView) view.findViewById(R.id.tv_title);
            view.setTag(holder);
        } else {
            holder = (FeedItemHolder) view.getTag();
        }
        FeedItem feed = getItem(i);
        holder.tv_title.setText(feed.title);
        holder.tv_description.setText(feed.description);
        return view;
    }
}