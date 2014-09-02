package com.localz.spotz.sdk.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.models.Spot;

public class MetadataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        Spot spot = (Spot) getIntent().getSerializableExtra(Spotz.EXTRA_SPOTZ);

        ListView metadataList = (ListView) findViewById(R.id.metadata_list);
        metadataList.setAdapter(new CustomAdapter(this, R.layout.listview_metadata_item, spot.metadata));
        metadataList.addHeaderView(getLayoutInflater().inflate(R.layout.listview_metadata_header, metadataList, false));
    }

    private class CustomAdapter extends ArrayAdapter<Spot.Metadata> {
        private final Spot.Metadata[] metadatas;

        public CustomAdapter(Context context, int resource, Spot.Metadata[] metadatas) {
            super(context, resource, metadatas);
            this.metadatas = metadatas;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.listview_metadata_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.key = (TextView) view.findViewById(R.id.key);
                viewHolder.value = (TextView) view.findViewById(R.id.value);
                view.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.key.setText(metadatas[position].key);
            viewHolder.value.setText(metadatas[position].value);

            return view;
        }
    }

    private class ViewHolder {
        public TextView key;
        public TextView value;
    }
}
