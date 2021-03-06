package jp.keitai2013.heallin.fragment;


import jp.crudefox.chikara.util.BitmapLruCache;
import jp.crudefox.chikara.util.CFCardUIAdapter;
import jp.crudefox.chikara.util.CFOverScrolledListView;
import jp.crudefox.chikara.util.CFUtil;
import jp.keitai2013.heallin.AppManager;
import jp.keitai2013.heallin.R;
import jp.keitai2013.heallin.chikara.manager.ClothesManager;
import jp.keitai2013.heallin.chikara.manager.ClothesManager.ClItem;
import jp.keitai2013.heallin.chikara.manager.LoginInfo;
import jp.keitai2013.heallin.chikara.manager.ProfileManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.extras.SoundPullEventListener;


/**
 * 		@author Chikara Funabashi
 * 		@date 2013/08/09
 *
 */

/**
 *
 *
 */

public class ClothesShopFragment extends SherlockFragment {



	private AppManager mApp;

//com.handmark.pulltorefresh.library.PullToRefreshListView

	private Context mContext;
	private View mRootView;

	private Handler mHandler = new Handler();

	//private CFOverScrolledListView mListView;
	private GridView mGridView;
	private PullToRefreshGridView mPullToRefreshGridView;

	//private LoginManager mLoginManager;
	//private TimeLineManager mTLManager;
	//private MemberManager mMemberManager;
	boolean mIsFirst = true;
	private ClothesManager mClothesManager;

	private LayoutInflater mLayoutInflater;
	//private DateFormat mDateFormat;


	private GetClothTask mGetClothTask;
	private BuyClothesTask mBuyHeallinTask;

	//private LoginInfo mLoginInfo;

	private ClothesGridViewAdapter mCAdapter;



	//private DeleteContributeTask mDelTask;

	private RequestQueue mQueue;
    private ImageLoader mImageLoader;



	public ClothesShopFragment() {
		super();
		setHasOptionsMenu(true);
	}




	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}


	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreateView(inflater, container, savedInstanceState);

		CFUtil.Log("onCreateView "+this);

		setContentView(R.layout.fragment_clothes);

		//mListView  = (CFOverScrolledListView) findViewById(R.id.member_frends_listView);
		mPullToRefreshGridView  = (PullToRefreshGridView) findViewById(R.id.clothes_gridview);
		GridView actualGridView = mGridView = mPullToRefreshGridView.getRefreshableView();

		float sd = getResources().getDisplayMetrics().density;


		mGridView.setNumColumns(3);
		mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		mGridView.setColumnWidth((int)(200*sd));


//		Bundle bundle = getArguments();

		//Intent intent = getIntent();
		//mLoginInfo = (LoginInfo) bundle.getSerializable(Const.AK_LOGIN_INFO);
		//mLoginInfo = mApp.getLoginInfo();

//		if(CFUtil.isOk_SDK(9)){
//			mListView.setOverscrollHeader(
//					getResources().getDrawable(R.drawable.update_over_scrolled));
//		}


//		mListView.setAdapter(mCAdapter);

//		if(mLoginInfo!=null){
//			//CFUtil.Log("length = "+mTLManager.getItemLength());
//			if(mMemberManager.getItemLength()==0){
//				postAttemptGetBorad(250);
//			}
//		}else{
//			toast("ログインに失敗しています。");
//			finish();
//		}

//		mListView.setOnOverScrolledListener(mOverScrolledListener);

		mPullToRefreshGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				ClItem mitem = mCAdapter.getItem(position);
				if(mitem==null) return ;
				showSelectDialog(mitem);

			}
		});


		//mListView.setOverscrollHeaderFooter(R.drawable.update_over_scrolled, 0);
//		mListView.setOverscrollHeaderEx(getResources().getDrawable(R.drawable.update_over_scrolled));
//		mListView.setOverscrollFooterEx(null);
//		mListView.setOnPullToRefreshListener(mPullToRefreshListener);


		mPullToRefreshGridView.setMode(PullToRefreshBase.Mode.BOTH);

		// Set a listener to be invoked when the list should be refreshed.
		mPullToRefreshGridView.setOnRefreshListener(new OnRefreshListener<GridView>() {
			@Override
			public void onRefresh(PullToRefreshBase<GridView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				//new GetDataTask().execute();
				attemptGetClothes();
			}
		});

		// Add an end-of-list listener
		mPullToRefreshGridView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				//Toast.makeText(PullToRefreshListActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
			}
		});

		// Need to use the Actual ListView when registering for Context Menu
		registerForContextMenu(actualGridView);

		/**
		 * Add Sound Event Listener
		 */
		SoundPullEventListener<GridView> soundListener = new SoundPullEventListener<GridView>(getActivity());
//		soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pull_event);
//		soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
//		soundListener.addSoundEvent(State.REFRESHING, R.raw.refreshing_sound);
		soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pyo1);
		soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
		soundListener.addSoundEvent(State.REFRESHING, R.raw.cat5);
		mPullToRefreshGridView.setOnPullEventListener(soundListener);

		// You can also just use setListAdapter(mAdapter) or
		// mPullRefreshListView.setAdapter(mAdapter)
		//actualListView.setAdapter(mCAdapter);

		mPullToRefreshGridView.setAdapter(mCAdapter);
		mCAdapter.notifyDataSetChanged();

		//updateListView();

		return mRootView;

	}


	private void showSelectDialog(final ClItem mitem){
		if(mitem==null) return ;


		AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());

		ab.setPositiveButton(android.R.string.ok, new  DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				attemptBuyHeallin(mitem.id);
			}
		});
		ab.setNegativeButton(android.R.string.cancel, new  DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});


		ab.setTitle("確認");
		ab.setMessage("商品を購入します。\nよろしいですか？");

		ab.create().show();


//		final AlertDialog[] dlg = new AlertDialog[1];
//
//		AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
//
//		//ab.setTitle(""+mitem.name);
//		//ab.setMessage("id="+mitem.id+" / name="+mitem.name+"");
//
//		View view = mLayoutInflater.inflate(R.layout.frend_simple_dialog, null);
//
//		ImageView icon_icon = (ImageView) view.findViewById(R.id.frend_simple_dlg_icon);
//		TextView text_id = (TextView) view.findViewById(R.id.frend_simple_dlg_id);
//		TextView text_name = (TextView) view.findViewById(R.id.frend_simple_dlg_name);
//		Button btn_del = (Button) view.findViewById(R.id.frend_simple_dlg_do_frend);
//
//		icon_icon.setImageBitmap(mitem.icon);
//		text_id.setText(""+mitem.id);
//		text_name.setText(""+mitem.name);
//
//		btn_del.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//
//				dlg[0].dismiss();
//
//				mMemberManager.removeItemById(mitem.id);
//				toast("サトシ"+"は \n"+
//						mitem.name+"を外に逃がしてあげた！\n"+
//						"バイバイ！"+mitem.name+"！");
//				updateListView();
//			}
//		});
//
//		ab.setView(view);
//
//		ab.setPositiveButton(android.R.string.ok, null);
//
//		dlg[0] = ab.create();
//		dlg[0].show();
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();

		mApp = (AppManager) getActivity().getApplication();

		mLayoutInflater = getLayoutInflater();

		mCAdapter = new ClothesGridViewAdapter(mContext);

		//mDateFormat = DateFormat.getInstance();

//		if(mLoginManager==null){
//			mLoginManager = new LoginManager(getApplicationContext());
//		}
//		if(mMemberManager==null){
//			mMemberManager = new MemberManager(getApplicationContext());
//		}

		//mLoginManager = mApp.getLoginManager();
		mClothesManager = new ClothesManager(getApplicationContext());

		mQueue = CFUtil.newRequestQueue(getActivity(), 32*1024*1024);
        mImageLoader = new ImageLoader(mQueue, new BitmapLruCache());

	}


	private View findViewById(int id){
		return mRootView.findViewById(id);
	}
	private LayoutInflater getLayoutInflater(){
		return getActivity().getLayoutInflater();
	}
	private void setContentView(int id){
		mRootView = getLayoutInflater().inflate(id, null);
	}
	private Context getApplicationContext(){
		return mContext.getApplicationContext();
	}

	private void finish(){
		Activity act = getActivity();
		if(act!=null){
			act.finish();
		}else{

		}
	}


//	private void postAttemptGetBorad(long delayed){
//		mHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				Activity activity = getActivity();
//				if(activity==null) return ;
//				if(activity.isFinishing()) return ;
//				attemptGetBorad();
//			}
//		}, delayed);
//	}

	private void attemptGetClothes(){
		if(mGetClothTask!=null){
			return ;
		}

		mGetClothTask = new GetClothTask();
		mGetClothTask.execute((Void)null);

	}

	private void attemptBuyHeallin(long id){
		if(mBuyHeallinTask!=null){
			return ;
		}

		mBuyHeallinTask = new BuyClothesTask();
		mBuyHeallinTask.execute(""+id);

	}

	public void updateListView(){

		mCAdapter.clearItems();

		for(int i=0;i<mClothesManager.getItemLength();i++){
			ClItem item = mClothesManager.getItemByIndex(i);
			mCAdapter.addItem(item, 0);
		}

		mCAdapter.notifyDataSetChanged();


	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.board, menu);
//		return true;
//	}



//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//
//		//Intent intent;
//
//		int id = item.getItemId();
//		switch(id){
//		case R.id.action_write:
//
//			//showContiributeDlg();
//			showContiributeActivity();
//
//			return true;
//		case R.id.action_update:
//
//			attemptGetBorad();
//
//			return true;
//		}
//
//		return false;
//	}

//	private static final int MENU_ID_UPDATE = 200;



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//super.onCreateOptionsMenu(menu, inflater);

		MenuItem mi;
		int order = 30;

//		mi = menu.add(Menu.NONE, Const.MENU_ID_UPDATE_MEMBER,order++,"更新");
//		mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		super.onCreateOptionsMenu(menu, inflater);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		return super.onOptionsItemSelected(item);

//		int id = item.getItemId();

//		if(id==Const.MENU_ID_UPDATE_MEMBER){
//			attemptGetBorad();
//			return true;
//		}

		return false;
	}




//	@Override
//	public void onBackPressed() {
//		// TODO 自動生成されたメソッド・スタブ
//		super.onBackPressed();
//	}




	@Override
	public void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDestroy();
		cancelTuusin();
	}



	@Override
	public void onPause() {
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();
	}

	@Override
	public void onStart() {
		// TODO 自動生成されたメソッド・スタブ
		super.onStart();

		if(mIsFirst){
			mIsFirst = false;

			mPullToRefreshGridView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mPullToRefreshGridView.setRefreshing(true);
				}
			}, 750);

		}

		mQueue.start();
	}

	@Override
	public void onStop() {
		super.onStop();

		mQueue.stop();
		cancelTuusin();
	}


	private void cancelTuusin(){
		if(mGetClothTask!=null){
			mGetClothTask.cancel(true);
		}
		if(mBuyHeallinTask!=null){
			mBuyHeallinTask.cancel(true);
		}
	}


	/**
	 * doBack, Progress, postExecute
	 */
	private class BuyClothesTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO: attempt authentication against a network service.

			postToast("購入中...");

			//boolean result = mMemberManager._update_mock(mLoginInfo);
			LoginInfo li = mApp.getLoginInfo();
			ProfileManager pm = mApp.getProfileManager();
			boolean result = pm.buyHeallin(li, Long.parseLong(params[0]));

			return result;

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			//showProgress(false);

			if(success){
				toast("購入しました。");
				finish();
			}else{
				toast("購入出来ませんでした\n(；´Д｀)");
			}

			mBuyHeallinTask = null;

		}

		@Override
		protected void onCancelled() {
			mBuyHeallinTask = null;
			//showProgress(false);
		}
	}


	/**
	 * doBack, Progress, postExecute
	 */
	private class GetClothTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			postToast("更新中...");

			//boolean result = mMemberManager._update_mock(mLoginInfo);
			LoginInfo li = mApp.getLoginInfo();
			boolean result = mClothesManager.updateShop(li);

			return result;

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			//showProgress(false);

			if(success){
				toast("更新しました。");
				updateListView();
			}else{
				toast("更新出来ませんでした\n(；´Д｀)");
			}

			mPullToRefreshGridView.onRefreshComplete();
			mGetClothTask = null;

		}

		@Override
		protected void onCancelled() {
			mGetClothTask = null;
			//showProgress(false);
		}
	}


	private class ClothesGridViewAdapter extends CFCardUIAdapter<ClItem>{


		public ClothesGridViewAdapter(Context context) {
			super(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View line = convertView;
			if(line==null){
				line =  mLayoutInflater.inflate(R.layout.clothes_grid_item, null);
			}

			ClItem mitem = getItem(position);

			//ListData data = mListData.get(position);

			TextView text1 = (TextView) line.findViewById(R.id.text1);
			TextView text2 = (TextView) line.findViewById(R.id.text2);
			final ImageView icon_icon = (ImageView) line.findViewById(R.id.icon);

//
			text1.setText("");
			text2.setText("");

			//icon_icon.setImageBitmap(mitem.image);
			ImageContainer ic1 = mImageLoader.get(mitem.image_url, new ImageListener() {

	            @Override
	            public void onErrorResponse(VolleyError error) {
	            	icon_icon.setImageResource(android.R.drawable.ic_dialog_alert);
	            }

	            @Override
	            public void onResponse(ImageContainer response, boolean isImmediate) {
	                if (response.getBitmap() != null) {
	                	Bitmap bmp = response.getBitmap();
	                    icon_icon.setImageBitmap(bmp);
	                } else {
	                    icon_icon.setImageResource(android.R.drawable.ic_menu_gallery);
	                }
	            }
	        }, 128, 128);



			if(isCardMotion(position)){
				doCardMotion( line,
						position%2==0 ? R.anim.card_ui_motion_from_left : R.anim.card_ui_motion_from_right);
				setCardMotion(position, false);
			}

			return line;
		}
	}








//	private class OnDelBtnClickListener implements View.OnClickListener{
//
//		int mmId;
//
//		public OnDelBtnClickListener(int id) {
//			mmId = id;
//		}
//
//		@Override
//		public void onClick(View v) {
//
//			if( mDelTask!=null ) return ;
//
//			final BItem bitem = mTLManager.getItemById(mmId);
//
//			AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
//			ab.setTitle("確認");
//			ab.setMessage("削除しますか。");
//
//			ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					mDelTask = new DeleteContributeTask();
//					mDelTask.execute(bitem.id);
//				}
//			});
//			ab.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//
//				}
//			});
//
//			ab.create().show();
//
//		}
//	}





//	public class DeleteContributeTask extends AsyncTask<Integer, Void, Boolean> {
//
//		@Override
//		protected Boolean doInBackground(Integer... params) {
//
//
//			//Toast.makeText(LoginActivity.this, "バックグラウンド処理中です！", Toast.LENGTH_SHORT).show();
//
//			//通信処理
//
//			//LoginInfo lf = mLoginManager.login(mId, mPassword);
//			boolean result = mMemberManager.deleteContoribute(mLoginManager , mLoginInfo, params[0]);
//
//			return result;
//		}
//
//		@Override
//		protected void onPostExecute(final Boolean result) {
//			mDelTask = null;
////			mAuthTask = null;
////			showProgress(false);
//
//
////			if (lf!=null) {
////
////				Intent data = new Intent();
////				data.putExtra(Const.AK_LOGIN_INFO, lf);
////				setResult(RESULT_OK, data);
////				finish();
////			} else {
////				mPasswordView
////						.setError(getString(R.string.error_incorrect_password));
////				mPasswordView.requestFocus();
////			}
//
//			if(result){
//				toast("削除しました。");
//				//mBoradManager.removeItemById(id)
//				mAdapter.notifyDataSetChanged();
//			}
//		}
//
//		@Override
//		protected void onCancelled() {
//			mDelTask = null;
//			//mAuthTask = null;
//			//showProgress(false);
//		}
//
//	}


//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//
//		if(requestCode==REQCODE_CONTRIBUTE_SUBMIT){
//			if(resultCode==RESULT_OK){
//
//				int id = data.getIntExtra(Const.AK_SUBMIT_CONTRIBUTE_ID, -1);
//				LoginInfo lf = (LoginInfo) data.getSerializableExtra(Const.AK_LOGIN_INFO);
//				if(lf!=null){
//					mLoginInfo = lf;
//				}
//
////				LoginInfo lf = (LoginInfo) data.getSerializableExtra(Const.AK_LOGIN_INFO);
////
////				Intent intent = new Intent(StartActivity.this, BoardActivity.class);
////				intent.putExtra(Const.AK_LOGIN_INFO, lf);
////
////				mMode = S_MODE_NONE;
////				startActivity(intent);
//
//				//attemptGetBorad();
//				postAttemptGetBorad(100);
//
//			}
//		}
//
//	}





	//private boolean mIsOverScrooling = false;


	private final CFOverScrolledListView.OnOverScrolledListener mOverScrolledListener = new CFOverScrolledListView.OnOverScrolledListener() {
		@Override
		public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {

			//CFUtil.Log(String.format("%d %d %s %s", scrollX, scrollY, clampedX, clampedY));

//			int thored = mListView.getMaxOverScrolledY()*50/100;
//			if(scrollY<-thored && !mIsOverScrooling && clampedY){
//				mIsOverScrooling = true;
//				attemptGetBorad();
//			}else if(scrollY>=0){
//				mIsOverScrooling = false;
//			}

		}
	};

	private final CFOverScrolledListView.OnPullToRefreshListener mPullToRefreshListener = new CFOverScrolledListView.OnPullToRefreshListener() {

		@Override
		public void onHeadRefresh() {
			attemptGetClothes();
		}

		@Override
		public void onFootRefresh() {


		}
	};




	private Toast mToast;
	private void toast(String str){
		if(mToast==null){
			mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
		}else{
			mToast.setText(str);
		}
		mToast.show();
	}
	private void postToast(final String str){
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				toast(str);
			}
		});
	}




}
