package audiorecord.messcat.com.mediorecorddemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import audiorecord.messcat.com.mediorecorddemo.LrcView.LrcView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    private LrcView lrcBig;
    private LrcView lrcSmall;
    private Button btnPlayPause, btnPlay;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler handler = new Handler();
    //线程操作
    private ExecutorService mExecutorService;
    //录音API
    private MediaRecorder mMediaRecorder;
    //录音开始时间与结束时间
    private long startTime, endTime;
    //录音所保存的文件
    private File mAudioFile;
    //录音文件保存位置
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio/";
    //当前是否正在录音
    private volatile boolean isPlaying = true;
    private int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //录音及播放要使用单线程操作
        mExecutorService = Executors.newSingleThreadExecutor();
        lrcBig = (LrcView) findViewById(R.id.lrc_big);
        lrcSmall = (LrcView) findViewById(R.id.lrc_small);
        btnPlayPause = (Button) findViewById(R.id.btn_play_pause);
        btnPlay = (Button) findViewById(R.id.btn_play);

        btnPlayPause.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        mediaPlayer.setOnCompletionListener(this);
        AssetManager am = getAssets();
        try {
            mediaPlayer.reset();
            AssetFileDescriptor fileDescriptor = am.openFd("cbg.mp3");
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lrcBig.loadLrc(getLrcText("cbg.lrc"));
        lrcSmall.loadLrc(getLrcText("cbg.lrc"));
    }

    private String getLrcText(String fileName) {
        String lrcText = null;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            lrcText = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lrcText;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play_pause:
                if (isPlaying) {
                    //安卓6.0以上录音相应权限处理
                    if (Build.VERSION.SDK_INT > 22) {
                        permissionForM();
                    } else {
                        startRecord();
                    }
                } else {
                    stopRecord();
                }
                break;
            case R.id.btn_play:
                if (mAudioFile.getPath() != null) {
                    Intent intent = new Intent(MainActivity.this, MakeVideoActivity.class);
                    intent.putExtra("uri", mAudioFile.getPath());
                    intent.putExtra("time",time);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "录音后才能播放", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /*******6.0以上版本手机权限处理***************************/
    /**
     * @description 兼容手机6.0权限管理
     * @author ldm
     * @time 2016/5/24 14:59
     */
    private void permissionForM() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            startRecord();
        }

    }

    /**
     * @description 开始进行录音
     * @author ldm
     * @time 2017/2/9 9:18
     */
    private void startRecord() {
        isPlaying = false;
        btnPlayPause.setText("完成录制");
        mediaPlayer.start();
        handler.post(runnable);
        //异步任务执行录音操作
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //播放前释放资源
                releaseRecorder();
                //执行录音操作
//                startRecords();
                recordOperation();
            }
        });
    }

    /**
     * @description 翻放录音相关资源
     * @author ldm
     * @time 2017/2/9 9:33
     */
    private void releaseRecorder() {
        if (null != mMediaRecorder) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * @description 录音操作
     * @author ldm
     * @time 2017/2/9 9:34
     */
    private void recordOperation() {
        //创建MediaRecorder对象
        mMediaRecorder = new MediaRecorder();
        //创建录音文件,.m4a为MPEG-4音频标准的文件的扩展名
        mAudioFile = new File(mFilePath + System.currentTimeMillis() + ".m4a");
        //创建父文件夹
        mAudioFile.getParentFile().mkdirs();
        try {
            //创建文件
            mAudioFile.createNewFile();
            //配置mMediaRecorder相应参数
            //从麦克风采集声音数据
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置保存文件格式为MP4
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //设置采样频率,44100是所有安卓设备都支持的频率,频率越高，音质越好，当然文件越大
            mMediaRecorder.setAudioSamplingRate(44100);
            //设置声音数据编码格式,音频通用格式是AAC
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置编码频率
            mMediaRecorder.setAudioEncodingBitRate(96000);
            //设置录音保存的文件
            mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());
            //开始录音
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            //记录开始录音时间
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description 结束录音操作
     * @author ldm
     * @time 2017/2/9 9:18
     */
    private void stopRecord() {
        isPlaying = true;
        btnPlayPause.setText("录音/暂停");
        mediaPlayer.pause();
        handler.removeCallbacks(runnable);
        //停止录音
        mMediaRecorder.stop();
        //记录停止时间
        endTime = System.currentTimeMillis();
        //录音时间处理，比如只有大于2秒的录音才算成功
        time = (int) ((endTime - startTime) / 1000);
        if (time >= 3) {
            //录音成功,添加数据
            //录音成功,发Message
        } else {
            mAudioFile = null;
        }
        //录音完成释放资源
        releaseRecorder();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        lrcBig.onDrag(0);
        lrcSmall.onDrag(0);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying()) {
                long time = mediaPlayer.getCurrentPosition();
                lrcBig.updateTime(time);
                lrcSmall.updateTime(time);
            }

            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }
}
