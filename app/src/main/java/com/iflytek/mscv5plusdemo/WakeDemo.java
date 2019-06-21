package com.iflytek.mscv5plusdemo;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.RequestListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;

public class WakeDemo extends Activity implements OnClickListener {
	private String TAG = "ivw";
	private Toast mToast;
	private TextView textView;
	// 语音唤醒对象
	private VoiceWakeuper mIvw;
	// 唤醒结果内容
	private String resultString;
	
	// 设置门限值 ： 门限值越低越容易被唤醒
	private TextView tvThresh;
	private SeekBar seekbarThresh;
	private final static int MAX = 3000;
	private final static int MIN = 0;
	public static int curThresh = 1450;
	private String threshStr = "门限值：";
	private String keep_alive = "1";
    private String ivwNetMode = "0";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.wake_activity);
        requestPermissions();

        startService(new Intent(this,WakeupService.class));

		initUi();
		// 初始化唤醒对象
		mIvw = VoiceWakeuper.createWakeuper(this, null);
	}

	@SuppressLint("ShowToast")
	private void initUi() {
		findViewById(R.id.btn_start).setOnClickListener(this);
		findViewById(R.id.btn_stop).setOnClickListener(this);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		textView = (TextView) findViewById(R.id.txt_show_msg);
		tvThresh = (TextView)findViewById(R.id.txt_thresh);
		seekbarThresh = (SeekBar)findViewById(R.id.seekBar_thresh);
		seekbarThresh.setMax(MAX - MIN);
		seekbarThresh.setProgress(curThresh);
		tvThresh.setText(threshStr + curThresh);
		seekbarThresh.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				curThresh = seekbarThresh.getProgress() + MIN;
				tvThresh.setText(threshStr + curThresh);
			}
		});
		
		RadioGroup group = (RadioGroup) findViewById(R.id.ivw_net_mode);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				/**
				 * 闭环优化网络模式有三种：
				 * 模式0：关闭闭环优化功能
				 * 
				 * 模式1：开启闭环优化功能，允许上传优化数据。需开发者自行管理优化资源。
				 * sdk提供相应的查询和下载接口，请开发者参考API文档，具体使用请参考本示例
				 * queryResource及downloadResource方法；
				 * 
				 * 模式2：开启闭环优化功能，允许上传优化数据及启动唤醒时进行资源查询下载；
				 * 本示例为方便开发者使用仅展示模式0和模式2；
				 */
				switch (arg1) {
				case R.id.mode_close:
					ivwNetMode = "0";
					break;
				case R.id.mode_open:
					ivwNetMode = "1";
					break;
				default:
					break;
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_start:
//			//非空判断，防止因空指针使程序崩溃
//			mIvw = VoiceWakeuper.getWakeuper();
//			if(mIvw != null) {
//				setRadioEnable(false);
//				resultString = "";
//				textView.setText(resultString);
//
//				// 清空参数
//				mIvw.setParameter(SpeechConstant.PARAMS, null);
//				// 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
//				mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"+ curThresh);
//				// 设置唤醒模式
//				mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
//				// 设置持续进行唤醒
//				mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
//				// 设置闭环优化网络模式
//				mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
//				// 设置唤醒资源路径
//				mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
//				// 设置唤醒录音保存路径，保存最近一分钟的音频
//				mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
//				mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );
//				// 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
//				//mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
//
//				// 启动唤醒
//				mIvw.startListening(mWakeuperListener);
//			} else {
//				showTip("唤醒未初始化");
//			}
			break;
		case R.id.btn_stop:
			mIvw.stopListening();
//			setRadioEnable(true);
			break;
		default:
			break;
		}		
	}
	

	


    private void requestPermissions(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.LOCATION_HARDWARE,Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS},0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}