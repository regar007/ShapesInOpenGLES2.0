package com.regar007.shapesinopengles20;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ListActivity
{
    private static final String ITEM_IMAGE = "item_image";
    private static final String ITEM_TITLE = "item_title";
    private static final String ITEM_SUBTITLE = "item_subtitle";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle(R.string.toc);
        setContentView(R.layout.table_of_contents);

        // Initialize data
        final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        final SparseArray<Class<? extends Activity>> activityMapping = new SparseArray<Class<? extends Activity>>();

        int i = 0;

        {
            final Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_IMAGE, R.drawable.points);
            item.put(ITEM_TITLE, getText(R.string.shape_one));
            item.put(ITEM_SUBTITLE, getText(R.string.shape_one_subtitle));
            data.add(item);
            activityMapping.put(i++, ShapeActivity.class);
        }

        {
            final Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_IMAGE, R.drawable.line);
            item.put(ITEM_TITLE, getText(R.string.shape_two));
            item.put(ITEM_SUBTITLE, getText(R.string.shape_two_subtitle));
            data.add(item);
            activityMapping.put(i++, ShapeActivity.class);
        }

        {
            final Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_IMAGE, R.drawable.triangle);
            item.put(ITEM_TITLE, getText(R.string.shape_three));
            item.put(ITEM_SUBTITLE, getText(R.string.shape_three_subtitle));
            data.add(item);
            activityMapping.put(i++, ShapeActivity.class);
        }

        {
            final Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_IMAGE, R.drawable.quad);
            item.put(ITEM_TITLE, getText(R.string.shape_four));
            item.put(ITEM_SUBTITLE, getText(R.string.shape_four_subtitle));
            data.add(item);
            activityMapping.put(i++, ShapeActivity.class);
        }

        {
            final Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_IMAGE, R.drawable.cube);
            item.put(ITEM_TITLE, getText(R.string.shape_five));
            item.put(ITEM_SUBTITLE, getText(R.string.shape_five_subtitle));
            data.add(item);
            activityMapping.put(i++, ShapeActivity.class);
        }

        {
            final Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_IMAGE, R.drawable.spheres);
            item.put(ITEM_TITLE, getText(R.string.shape_six));
            item.put(ITEM_SUBTITLE, getText(R.string.shape_six_subtitle));
            data.add(item);
            activityMapping.put(i++, ShapeActivity.class);
        }

        {
            final Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_IMAGE, R.drawable.heightmap);
            item.put(ITEM_TITLE, getText(R.string.shape_seven));
            item.put(ITEM_SUBTITLE, getText(R.string.shape_seven_subtitle));
            data.add(item);
            activityMapping.put(i++, ShapeActivity.class);
        }

        final SimpleAdapter dataAdapter = new SimpleAdapter(this, data, R.layout.toc_item, new String[] {ITEM_IMAGE, ITEM_TITLE, ITEM_SUBTITLE}, new int[] {R.id.Image, R.id.Title, R.id.SubTitle});
        setListAdapter(dataAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                final Class<? extends Activity> activityToLaunch = activityMapping.get(position);
                ShapeActivity.shape = position;

                if (activityToLaunch != null)
                {
                    final Intent launchIntent = new Intent(MainActivity.this, activityToLaunch);
                    startActivity(launchIntent);
                }
            }
        });
    }
}
