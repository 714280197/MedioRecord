package audiorecord.messcat.com.mediorecorddemo;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import audiorecord.messcat.com.mediorecorddemo.LrcView.MyVisualizerView;

/**
 * Created by Administrator on 2017/9/18 0018.
 */

public class PlayActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;//环绕音
    private Equalizer equalizer;//均匀器
    private BassBoost bassBoost;//重低音
    private PresetReverb presetReverb;//预设音场控制器
    private LinearLayout layout;
    private ScrollView scrollView;
    private List<Short> reverbNames = new ArrayList<Short>();
    private List<String> reverbVals = new ArrayList<String>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //layout = new LinearLayout(this);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        scrollView = new ScrollView(this);
        scrollView.addView(layout);

        setContentView(scrollView);
        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(new File(getIntent().getStringExtra("uri"))));
        //初始化滤波器
        setupVisualizer();
        //初始化均衡控制器
        setupEqualizer();
        //初始化重低音控制器
        setupBassBoost();
        //初始化预设音场控制器
        setupPresetReverb();
        mediaPlayer.start();
    }
    public void setupPresetReverb() {
        presetReverb = new PresetReverb(0, mediaPlayer.getAudioSessionId());
        presetReverb.setEnabled(true);
        TextView prTitle = new TextView(this);
        prTitle.setText("音场");
        layout.addView(prTitle);

        for (short i = 0; i < equalizer.getNumberOfPresets(); i++) {
            reverbNames.add(i);
            reverbVals.add(equalizer.getPresetName(i));
        }

        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reverbVals));
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                presetReverb.setPreset(reverbNames.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });
        layout.addView(sp);

    }

    public void setupBassBoost() {
        bassBoost = new BassBoost(0, mediaPlayer.getAudioSessionId());
        bassBoost.setEnabled(true);
        TextView bbTitle = new TextView(this);
        bbTitle.setText("重低音：");
        layout.addView(bbTitle);

        SeekBar bar = new SeekBar(this);
        //重低音范围为0-1000
        bar.setMax(1000);
        bar.setProgress(0);

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                bassBoost.setStrength((short) progress);
            }
        });
        layout.addView(bar);
    }

    public void setupEqualizer() {
        equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
        equalizer.setEnabled(true);
        TextView eqTitle = new TextView(this);
        eqTitle.setText("均衡器：");
        layout.addView(eqTitle);
        //获取均衡器控制器支持最小值和最大值
        final short minEQLevel = equalizer.getBandLevelRange()[0];
        short maxEQLevel = equalizer.getBandLevelRange()[1];

        //获取均衡器控制器支持的所有频率
        short brands = equalizer.getNumberOfBands();
        for (short i = 0; i < brands; i++) {
            TextView eqTextView = new TextView(this);
            eqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            eqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            eqTextView.setText((equalizer.getCenterFreq(i) / 1000) + "Hz");
            layout.addView(eqTextView);

            LinearLayout tmpLayout = new LinearLayout(this);
            tmpLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            minDbTextView.setText((minEQLevel / 100) + "dB");

            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            maxDbTextView.setText((maxEQLevel / 100) + "dB");

            SeekBar bar = new SeekBar(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(equalizer.getBandLevel(i));
            final short brand = i;

            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    equalizer.setBandLevel(brand, (short) (progress + minEQLevel));
                }
            });
            //使用水平排列组件的LinearLayout盛装三个组件
            tmpLayout.addView(minDbTextView);
            tmpLayout.addView(bar);
            tmpLayout.addView(maxDbTextView);
            layout.addView(tmpLayout);
        }


    }


    public void setupVisualizer() {
        final MyVisualizerView myVisualizerView = new MyVisualizerView(this);
        myVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (120f * getResources().getDisplayMetrics().density)));
        layout.addView(myVisualizerView);
        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform,
                                              int samplingRate) {
                // TODO Auto-generated method stub
                //String str = new String(waveform, 0, waveform.length);
                System.out.println("capture->" + waveform.length);
                /*
                for (byte b: waveform) {
					System.out.println(b);
				}*/
                myVisualizerView.updateVisualizer(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft,
                                         int samplingRate) {
                // TODO Auto-generated method stub

            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
        visualizer.setEnabled(true);

    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if (isFinishing() && mediaPlayer != null) {
            visualizer.release();
            equalizer.release();
            presetReverb.release();
            bassBoost.release();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onPause();
    }
}
