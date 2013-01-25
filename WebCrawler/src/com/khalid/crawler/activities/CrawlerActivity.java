package com.khalid.crawler.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.khalid.crawler.ApplicationEx;
import com.khalid.crawler.interfaces.ICrawlerReportable;
import com.khalid.crawler.interfaces.IPoolReportable;
import com.khalid.crawler.queue.URLPool;
import com.khalid.crawler.threads.GetLinksService;
import com.khalid.crawler.util.Utility;
import com.khalid.webcrawler.R;

public class CrawlerActivity extends Activity implements ICrawlerReportable,
		IPoolReportable {

	private static final String TAG = "CrawlerActivity";
	private EditText mUrl;
	private Button mButton1;
	private TextView mCurrentLinkText;
	private TextView mLinksCountText;
	private TextView mProcessedLinksCountText;
	private String mPageURL;
	private boolean mIsActive = false;
	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crawler);

		// initializing UI elements
		mUrl = (EditText) findViewById(R.id.urlEditText);
		mButton1 = (Button) findViewById(R.id.beginButton);
		mCurrentLinkText = (TextView) findViewById(R.id.currentLinkTextView);
		mLinksCountText = (TextView) findViewById(R.id.goodLinkTextView);
		mProcessedLinksCountText = (TextView) findViewById(R.id.badLinkTextView);
		URLPool.getInstance().setPoolListener(this);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
	}
	
	

	public void beginButtonClicked(View view) {
		
		if (!((Utility.validatURL(mUrl.getText().toString(), this)&&(Utility.isIntenetAvailable(this))))) {
			return;
		}
		mPageURL = mUrl.getText().toString();
		if (mIsActive) {
			stopCrawling();
		} else {
			mIsActive = true;
			mButton1.setText(R.string.cancle_text);
			Toast.makeText(this, "Starting Crawler", Toast.LENGTH_SHORT).show();
			URLPool.getInstance().push(mPageURL);
			startCrawling();
		}
	}

	

	private void startCrawling() {
		try {
			/**
			 * before reconstructing ThreadPoolExecutor do some validation
			 * 1> Was shutdown called with proper flags
			 * 	<a> start crawling flag (mIsActive) should be set and pool size should be less than its limit
			*/
			if ((ApplicationEx.operationsQueue.isShutdown()) && (mIsActive)) {
				// calling start again 
				ApplicationEx.reconstructThreadPool();
			}
			progressStart();
			/**
			 * check if pool has URLs to be crawled
			 * 
			*/
			String pageURL = URLPool.getInstance().pop();
			if (!TextUtils.isEmpty(pageURL)) {
				Log.v("startCrawling", pageURL);
				GetLinksService service = new GetLinksService(pageURL, this);
				ApplicationEx.operationsQueue.execute(service);
			}else{
				// either pool is empty or pool limit has reached
				Toast.makeText(this,
						"Either Queue is empty or its size limit reached",
						Toast.LENGTH_SHORT).show();
						stopCrawling();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.v("startCrawling", e.getLocalizedMessage());
		}
	}

	private void stopCrawling() {
		// avoid execution when crawling has already been interrupted by crawler itself due to Queue limit reached or empty Queue
		if (!ApplicationEx.operationsQueue.isShutdown()) {
			mIsActive = false;
			mButton1.setText(R.string.begin_text);
			Toast.makeText(this, "Crawler Shutting down", Toast.LENGTH_SHORT)
					.show();
			ApplicationEx.operationsQueue.shutdownNow();
			ApplicationEx.operationsQueue.getQueue().clear();

			// current link count
			mLinksCountText.setText(getString(R.string.links_count) + " "
					+ URLPool.getInstance().getUrlPoolSize());

			// current processed pool count
			mProcessedLinksCountText
					.setText(getString(R.string.processed_links_count) + " "
							+ URLPool.getInstance().getUrlProcessedPoolSize());
			Log.e("link count"," count "+URLPool.getInstance().getUrlPoolSize());
			Log.e("processed link count"," count "+URLPool.getInstance().getUrlProcessedPoolSize());
			progressStop();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_crawler, menu);
		return true;
	}

	@Override
	public void spiderFoundURL(String urlString) {
		Message message = Message.obtain();
		message.what = 1;
		message.obj = (String) urlString;
		crawlerHandler.sendMessage(message);
	}
	@Override
	public void spiderURLProcessed(int processedURLCount) {
		// Log.v(TAG, "spiderURLProcessed " + processedURLCount);
		Message message = Message.obtain();
		message.what = 3;
		message.obj = (int) processedURLCount;
		crawlerHandler.sendMessage(message);

	}

	@Override
	public void spiderLinkCounts(int goodLinkCount) {
		// Log.v(TAG, "spiderGoodLinkCounts");
		Message message = Message.obtain();
		message.what = 2;
		message.obj = (int) goodLinkCount;
		crawlerHandler.sendMessage(message);

	}

	public android.os.Handler crawlerHandler = new android.os.Handler() {
		public void handleMessage(Message msg) {

			// current URL
			if (msg.what == 1) {
				mCurrentLinkText.setText(getString(R.string.current_link) + " "
						+ (String) msg.obj);
			}
			// current link count
			if (msg.what == 2)
				mLinksCountText.setText(getString(R.string.links_count) + " "
						+ msg.obj);
			// processed link count
			if (msg.what == 3) {
				mProcessedLinksCountText
						.setText(getString(R.string.processed_links_count)
								+ " " + msg.obj);
			}

			// pool size reached
			if (msg.what == 4) {
				stopCrawling();
			}
			if (msg.what == 5){
				
				// don't call if shutdown has been called, if stopCrawling flag is active and pool size has reached its limit
				if((!ApplicationEx.operationsQueue.isShutdown())&&(mIsActive)&&(!URLPool.getInstance().hasPoolSizeReached())){
					startCrawling();
				}
			}
				
			
		};
	};

	
	@Override
	public void finished() {
		Log.v(TAG, "finished");
		Message message = Message.obtain();
		message.what = 5;
		crawlerHandler.sendMessage(message);
	}

	@Override
	public void onPoolSizeReached() {
		Log.v(TAG, "onPoolSizeReached");
		Message message = Message.obtain();
		message.what = 4;
		crawlerHandler.sendMessage(message);
	}
	
	/**
	 * start progress bar 
	 * 
	*/
	private void progressStart(){
		mProgressBar.setVisibility(View.VISIBLE);
	}
	/**
	 * stop progress bar 
	 * 
	*/
	private void progressStop(){
		mProgressBar.setVisibility(View.INVISIBLE);
	}
}
