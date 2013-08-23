package com.maomuffy.miinglegcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import static com.maomuffy.miingleutilities.GCMPoCUtilities.TAG;
import com.maomuffy.miingleutilities.DBCategoryAdapter;

public class CategoriesActivity extends Activity implements OnItemClickListener {

	ListView lvCategoryList;
	Cursor c;
	DBCategoryAdapter miingleDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories_activity);
		miingleDb = new DBCategoryAdapter(getApplicationContext());
		miingleDb.open();
		DisplayList(this);
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		miingleDb.close();
	}

	private void DisplayList(Context ctx) {
		lvCategoryList = (ListView) findViewById(R.id.lvCategoryList);

		String[] from = { DBCategoryAdapter.KEY_CATEGORY_NAME };
		int[] to = { R.id.tvCategoryName };
		c = miingleDb.getAllRows();

		SimpleCursorAdapter cAdapter = new SimpleCursorAdapter(ctx,
				R.layout.category_list_view, c, from, to, 0);
		
		lvCategoryList.setAdapter(cAdapter);
		lvCategoryList.setOnItemClickListener(this);
		
		startManagingCursor(c); // Replace with CursorLoader implementation
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long dbId) {
		c = miingleDb.getRow(dbId);
		if(c.moveToFirst()){
			String categoryId = c.getString(DBCategoryAdapter.COL_CATEGORY_ID);
			Log.i(TAG, categoryId);
			
			Intent discussion = new Intent(this, DiscussionsActivity.class);
			discussion.putExtra("category_id", categoryId);
			discussion.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(discussion);
			
		}
	}
	

}
