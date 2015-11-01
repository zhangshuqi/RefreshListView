package com.itheima13.refreshlistview;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;



public class RefreshListView extends ListView {

	private LinearLayout headRoot;
	private LinearLayout mRefreshHeadView;
	private View mViewFoot;
	private int mRefreshHeadHeight;
	private int mViewFootHeight;
	private float downY = -1;
	private View m_lunbo;

	private static final int PULLDOWN_STATE = 1;// 下拉刷新
	private static final int RELEASE_STATE = 2; // 松开刷新
	private static final int REFRSHING_STATE = 3; // 正在刷新
	private int refreshState = PULLDOWN_STATE;// 初始状态为下拉刷新
	private ImageView iv_arrow;
	private ProgressBar pb_loading;
	private TextView tv_headstateDesc;
	private TextView tv_refreshtime;
	private RotateAnimation ra_down;
	private RotateAnimation ra_up;
	private boolean isLoadingMore = false;//是否正在加载更多数据

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		// 1. 初始化头
		initHead();
		// 2. 初始化尾
		initFoot();
		// 3. 初始化动画
		initAnimation();
		
		//4. 初始化事件
		initEvent();
	}
	

	private void initEvent() {
		//给lv添加滑动事件
		this.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// 监听是否滑动到最后一条数据
				
				// 静止状态
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					//是否滑动到最后一条数据
					//PrintLog.print(getLastVisiblePosition() + "<>" + getAdapter().getCount());
					
					if (getLastVisiblePosition() == getAdapter().getCount() - 1 && !isLoadingMore) {
						isLoadingMore = true;
						
						//界面的显示
						mViewFoot.setPadding(0, 0, 0, 0);
						
						//设置加载更多数据的界面显示 
						setSelection(getAdapter().getCount());
						//加载更多
						//PrintLog.print("加载更多");
						if (mOnRefreshDataListener != null) {
							mOnRefreshDataListener.loadMore();
						}
						
						
						
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// 覆盖touch事件
		// 拖动事件处理
		/*
		 * listview显示第一个数据 拖动出下拉刷新的View 从上往下拖动
		 */
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 按下

			downY = ev.getY();

			break;

		case MotionEvent.ACTION_MOVE:
			
			// 如果处于正在刷新状态
			if (refreshState == REFRSHING_STATE) {
				return true;//屏蔽事件
			}

			/*if (!isLunboShow()) {
				// 没有完全显示
				break;
			}*/
			// 拖动
			if (downY == -1) {
				downY = ev.getY();
			}

			float moveY = ev.getY();

			float dy = moveY - downY;

			// listview显示第一个数据 拖动出下拉刷新的View 从上往下拖动
			// 轮播图完全显示 才相应拖动事件
			if (getFirstVisiblePosition() == 0 && dy > 0) {
				// 显示刷新view
				/*
				 * PrintLog.print("拖出刷新view"); float hiddenHeight =
				 * -mRefreshHeadHeight + dy; mRefreshHeadView.setPadding(0,
				 * (int) hiddenHeight, 0, 0);
				 */
				// 处理状态
				float hiddenHeight = -mRefreshHeadHeight + dy;

				if (hiddenHeight >= 0 && refreshState != RELEASE_STATE) {
					refreshState = RELEASE_STATE;
					processState();
				} else if (hiddenHeight < 0 && refreshState != PULLDOWN_STATE) {
					refreshState = PULLDOWN_STATE;
					processState();
				}

				// 拖动位置的改变
				mRefreshHeadView.setPadding(0, (int) hiddenHeight, 0, 0);
				return true;
			}

			break;

		case MotionEvent.ACTION_UP:
			// 松开
			// 判断状态
			// 下拉刷新
			if (refreshState == PULLDOWN_STATE) {
				// 继续隐藏
				mRefreshHeadView.setPadding(0, -mRefreshHeadHeight, 0, 0);
			} else if (refreshState == RELEASE_STATE) {
				// 松开刷新
				refreshState = REFRSHING_STATE;
				processState();
				
				//刷新数据业务调用
				if (mOnRefreshDataListener != null) {
					mOnRefreshDataListener.freshData();
				}
				mRefreshHeadView.setPadding(0, 0, 0, 0);
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 更新状态
	 */
	public void updateState(){
		System.out.println("isLoadingMore:" + isLoadingMore);
		if (isLoadingMore) {
			//加载更多
			mViewFoot.setPadding(0, -mViewFootHeight, 0, 0);
			isLoadingMore = false;
		} else {
			//下拉刷新
			updateRefreshState();
		}
	}
	
	public void updateRefreshState(){
		//改变状态 下拉刷新
		refreshState = PULLDOWN_STATE;
		//显示箭头
		iv_arrow.setVisibility(View.VISIBLE);
		//隐藏进度条
		pb_loading.setVisibility(View.GONE);
		//改变文字 
		tv_headstateDesc.setText("下拉刷新");
		//设置刷新时间
		tv_refreshtime.setText(getCurrentTime());
		//隐藏刷新的view
		mRefreshHeadView.setPadding(0, -mRefreshHeadHeight, 0, 0);
	}
	
	private String getCurrentTime(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}
	
	/**
	 * 初始化箭头的动画
	 */
	private void initAnimation(){
		ra_up = new RotateAnimation(0, -180,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		ra_up.setDuration(500);
		ra_up.setFillAfter(true);//动画结束位置
		
		ra_down = new RotateAnimation(-180, -360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		ra_down.setDuration(500);
		ra_down.setFillAfter(true);//动画结束位置
	}
	
	private OnRefreshDataListener mOnRefreshDataListener;
	public void setOnRefreshDataListener(OnRefreshDataListener listener) {
		mOnRefreshDataListener = listener;
	}
	
	public interface OnRefreshDataListener{
		//刷新数据
		void freshData();
		
		//加载更多
		void loadMore();
		
	}

	private void processState() {
		switch (refreshState) {
		case PULLDOWN_STATE:
			// 下拉刷新状态
			//PrintLog.print("下拉刷新状态");
			//箭头向下动画
			iv_arrow.startAnimation(ra_down);
			//文字的切换
			tv_headstateDesc.setText("下拉刷新");
			break;
		case RELEASE_STATE:
			// 松开刷新状态
			//PrintLog.print("松开刷新状态");
			//箭头向上动画
			iv_arrow.startAnimation(ra_up);
			//文字的切换
			tv_headstateDesc.setText("松开刷新");
			break;
		case REFRSHING_STATE:
			// 正在刷新状态
			//PrintLog.print("正在刷新状态");
			//清除动画
			iv_arrow.clearAnimation();
			//隐藏箭头
			iv_arrow.setVisibility(View.GONE);
			//显示进度
			pb_loading.setVisibility(View.VISIBLE);
			//改变文件
			tv_headstateDesc.setText("正在刷新");
			break;

		default:
			break;
		}
	}

	public void addLunBo(View v_lunbo) {
		m_lunbo = v_lunbo;
		headRoot.addView(v_lunbo);
	}

	/**
	 * 轮播图是否完全显示
	 * 
	 * @return
	 */
	public boolean isLunboShow() {
		int[] location = new int[2];
		// 获取listview在屏幕中的坐标
		this.getLocationInWindow(location);

		// listview在屏幕中的y坐标
		int lv_y = location[1];

		// 获取轮播图在屏幕中的位置
		m_lunbo.getLocationInWindow(location);
		int lunbo_y = location[1];

		if (lunbo_y >= lv_y) {
			return true;
		} else {
			return false;
		}

	}

	private void initHead() {
		headRoot = (LinearLayout) View.inflate(getContext(),
				R.layout.listview_head, null);

		mRefreshHeadView = (LinearLayout) headRoot
				.findViewById(R.id.ll_listview_head_refreshview);
		
		
		//获取子控件
		
		iv_arrow = (ImageView) headRoot.findViewById(R.id.iv_listview_head_arrow);
		pb_loading = (ProgressBar) headRoot.findViewById(R.id.pb_listview_head_loading);
		tv_headstateDesc = (TextView) headRoot.findViewById(R.id.tv_listview_head_statedesc);
		tv_refreshtime = (TextView) headRoot.findViewById(R.id.tv_listview_head_time);

		mRefreshHeadView.measure(0, 0);

		mRefreshHeadHeight = mRefreshHeadView.getMeasuredHeight();
		// 隐藏下拉刷新头
		mRefreshHeadView.setPadding(0, -mRefreshHeadHeight, 0, 0);

		addHeaderView(headRoot);

	}

	private void initFoot() {
		mViewFoot = View.inflate(getContext(), R.layout.listview_foot, null);
		// 隐藏加载更多footer
		mViewFoot.measure(0, 0);
		mViewFootHeight = mViewFoot.getMeasuredHeight();

		mViewFoot.setPadding(0, -mViewFootHeight, 0, 0);
		addFooterView(mViewFoot);
	}

	public RefreshListView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

}
