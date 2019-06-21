package com.iflytek.mscv5plusdemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class WakeupService extends Service {

    private String TAG = "WakeupService";
//    private TextView textView;
    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    // 唤醒结果内容
    private String resultString;

    private String keep_alive = "1";
    private String ivwNetMode = "0";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        startListening();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy WakeDemo");
        stopListening();
        // 销毁合成对象
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
        }
    }


    //开始监听语音输入
    public void startListening(){
        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if(mIvw != null) {
//            setRadioEnable(false);
            resultString = "";
//            textView.setText(resultString);

            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"+ WakeDemo.curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
            mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            mIvw.startListening(mWakeuperListener);
        } else {
            showTip("唤醒未初始化");
        }
    }

    //停止监听语音输入
    public void stopListening(){
        mIvw.stopListening();
//        setRadioEnable(true);
    }




    /**
     * 查询闭环优化唤醒资源
     * 请在闭环优化网络模式1或者模式2使用
     */
    public void queryResource() {
        int ret = mIvw.queryResource(getResource(), requestListener);
        showTip("updateResource ret:"+ret);
    }


    // 查询资源请求回调监听
    private RequestListener requestListener = new RequestListener() {
        @Override
        public void onEvent(int eventType, Bundle params) {
            // 以下代码用于获取查询会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //if(SpeechEvent.EVENT_SESSION_ID == eventType) {
            // 	Log.d(TAG, "sid:"+params.getString(SpeechEvent.KEY_EVENT_SESSION_ID));
            //}
        }

        @Override
        public void onCompleted(SpeechError error) {
            if(error != null) {
                Log.d(TAG, "error:"+error.getErrorCode());
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            try {
                String resultInfo = new String(buffer, "utf-8");
                Log.d(TAG, "resultInfo:"+resultInfo);

                JSONTokener tokener = new JSONTokener(resultInfo);
                JSONObject object = new JSONObject(tokener);

                int ret = object.getInt("ret");
                if(ret == 0) {
                    String uri = object.getString("dlurl");
                    String md5 = object.getString("md5");
                    Log.d(TAG,"uri:"+uri);
                    Log.d(TAG,"md5:"+md5);
                    showTip("请求成功");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Log.d(TAG, "onResult");
            if(!"1".equalsIgnoreCase(keep_alive)) {
//                setRadioEnable(true);
            }
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);

//                StringBuffer buffer = new StringBuffer();
//                buffer.append("【RAW】 "+text);
//                buffer.append("\n");
//                buffer.append("【操作类型】"+ object.optString("sst"));
//                buffer.append("\n");
//                buffer.append("【唤醒词id】"+ object.optString("id"));
//                buffer.append("\n");
//                buffer.append("【得分】" + object.optString("score"));
//                buffer.append("\n");
//                buffer.append("【前端点】" + object.optString("bos"));
//                buffer.append("\n");
//                buffer.append("【尾端点】" + object.optString("eos"));
//                resultString =buffer.toString();

                //判断唤醒得分是否 >= 阈值（唤醒成功）
                if (Integer.valueOf(object.optString("score")) >= WakeDemo.curThresh){
                    //通过包名启动目标应用（小爱同学）
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.miui.voiceassist");
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    showTip("唤醒成功");
                    Log.i(TAG, "唤醒成功 ");
                }
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
//            textView.setText(resultString);
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
//            setRadioEnable(true);
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch( eventType ){
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray( SpeechEvent.KEY_EVENT_RECORD_DATA );
                    Log.i( TAG, "ivw audio length: "+audio.length );
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };


    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(getApplicationContext(), ResourceUtil.RESOURCE_TYPE.assets, "ivw/"+getString(R.string.app_id)+".jet");
        Log.d( TAG, "resPath: "+resPath );
        return resPath;
    }

    private void showTip(final String str) {
        Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mToast.setText(str);
//                mToast.show();
//            }
//        });
    }

//    private void setRadioEnable(final boolean enabled) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                findViewById(R.id.ivw_net_mode).setEnabled(enabled);
//                findViewById(R.id.btn_start).setEnabled(enabled);
//                findViewById(R.id.seekBar_thresh).setEnabled(enabled);
//            }
//        });
//    }



}
