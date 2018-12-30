
package com.example.asus.mykougoumusic;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.MusicData.Music;
import com.example.asus.MusicData.MusicList;
import com.example.asus.model.PropertyBean;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Build.VERSION_CODES.M;
import static com.example.asus.mykougoumusic.R.id.ib9;

public class MyMusic extends AppCompatActivity implements OnGestureListener, View.OnClickListener, OnMenuItemClickListener {

    // 显示组件
    private ImageButton imgBtn_Previous;
    private ImageButton imgBtn_PlayOrPause;
    //private ImageButton imgBtn_Stop;
    private ImageButton imgBtn_Next;
    private ImageButton model3;
    private ImageButton imageButton9;
    private ListView list;


    //歌曲列表对象
    private ArrayList<Music> musicArrayList;

    //T退出判断标志
    private static boolean isExit = false;

    // 当前歌曲的序号，下标从0开始
    private int number = 0;
    // 播放状态
    private int status;
    private static int a1 = 0;


    // 广播接收器
    private StatusChangedReceiver receiver;
    private RelativeLayout root_Layout;
    private LinearLayout linearLayout1;
    private LinearLayout main_volumeLayout;


    private TextView text_Current;
    private TextView text_Duration;
    private SeekBar seekBar;
    private TextView textView;
    private TextView textView5;

    private Handler seekBarHandler;
    //当前歌曲的持续时间和当前位置，作用于进度条
    private int duration;
    private int time;

    //进度条控制常量
    private static final int PROGRESS_INCREASE = 0;
    private static final int PROGRESS_PAUSE = 1;
    private static final int PROGRESS_RESET = 2;

    //播放模式常量
    private static final int MODE_LIST_SEQUENCE = 0;
    private static final int MODE_SINGLE_CYCLE = 1;
    private static final int MODE_LIST_CYCLE = 2;
    private static final int MODE_LIST_RANDOM = 3;
    private int playmode;

    //音量控制
    private TextView tv_vol;
    private SeekBar seekbar_vol;

    //音乐信息
    public static String musicName ;
    public static String musicArtist ;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private GestureDetector gestureDetector;

    private static final int REQUEST_PERMISSION = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.layout_my_music);
        gestureDetector = new GestureDetector(this, this);


        //动态获取权限
        if (Build.VERSION.SDK_INT >= M) {

            int hasWritePermission = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<String>();
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {

            }
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);

            } else {

            }
            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                                .WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_PHONE_STATE},
                        REQUEST_PERMISSION);
            }
        }
        findViews();//获取显示组件
        registerForContextMenu(textView);//注册上下文菜单
        //弹出式菜单
        imageButton9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MyMusic.this, view);
                popupMenu.inflate(R.menu.menu);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_theme:
                                new AlertDialog.Builder(MyMusic.this).setTitle("请选择主题").setItems(R.array.theme, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String theme = PropertyBean.THEMES[which];
                                        MyMusic.this.setTheme(theme);
                                        PropertyBean property = new PropertyBean(MyMusic.this);
                                        property.setAndSaveTheme(theme);
                                    }
                                }).show();
                                Toast.makeText(MyMusic.this, "你选择了" + getResources().getString(R.string.theme), Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.menu_about:
                                new AlertDialog.Builder(MyMusic.this)
                                        .setTitle("GRacePlayer")
                                        .setMessage(R.string.about2).show();
                                break;
                            case R.id.menu_quit:
                                //退出程序
                                new AlertDialog.Builder(MyMusic.this)
                                        .setTitle("提示").
                                        setMessage(R.string.quit).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        System.exit(0);
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                    }
                                }).show();
                                break;

                        }
                        return false;
                    }
                });
            }
        });

        registerListeners();//为显示组件注册监听器
        initMusicList();//初始化音乐列表对象
        initListView();//设置适配器并初始化listView
        checkMusicfile();//如果列表没有歌曲，则播放按钮不可用，并提醒用户
        duration = 0;
        time = 0;
        bindStatusChangedReceiver();// 绑定广播接收器，可以接收广播
        initSeekBarHandler();//更新进度条的三个状态
        startService(new Intent(this, MusicService.class));
        status = MusicService.COMMAND_STOP;

        //默认播放模式为顺序播放
        playmode = MyMusic.MODE_LIST_SEQUENCE;
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /**
     * 绑定广播接收器
     */
    private void bindStatusChangedReceiver() {
        receiver = new StatusChangedReceiver();
        IntentFilter filter = new IntentFilter(
                MusicService.BROADCAST_MUSICSERVICE_UPDATE_STATUS);
        registerReceiver(receiver, filter);
    }

    /**
     * 获取显示组件
     */
    private void findViews() {
        imgBtn_Previous = (ImageButton) findViewById(R.id.imageButton1);
        imgBtn_PlayOrPause = (ImageButton) findViewById(R.id.imageButton2);
        //imgBtn_Stop = (ImageButton) findViewById(R.id.imageButton3);
        imgBtn_Next = (ImageButton) findViewById(R.id.imageButton4);

        imageButton9 = (ImageButton) findViewById(ib9);

        model3 = (ImageButton) findViewById(R.id.imageButton3);

        list = (ListView) findViewById(R.id.listView1);
        root_Layout = (RelativeLayout) findViewById(R.id.relativeLayout1);
        linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
        main_volumeLayout = (LinearLayout) findViewById(R.id.main_volumeLayout);


        textView = (TextView) findViewById(R.id.textView);
        textView5 = (TextView) findViewById(R.id.textView5);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        text_Current = (TextView) findViewById(R.id.textView1);
        text_Duration = (TextView) findViewById(R.id.textView2);
        tv_vol = (TextView) findViewById(R.id.main_tv_volumeText);
        seekbar_vol = (SeekBar) findViewById(R.id.main_sb_volumebar);
    }

    /**
     * 为显示组件注册监听器
     */
    private void registerListeners() {
        //在界面点击下一首时，执行以下代码
        imgBtn_Previous.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switch (playmode) {
                    case MyMusic.MODE_LIST_RANDOM://随机播放
                        sendBroadcastOnCommand(MusicService.COMMAND_RANDOM);
                        break;

                    default:
                        sendBroadcastOnCommand(MusicService.COMMAND_PREVIOUS);
                }
            }
        });

        imgBtn_PlayOrPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switch (status) {
                    case MusicService.STATUS_PLAYING:
                        sendBroadcastOnCommand(MusicService.COMMAND_PAUSE);
                        break;
                    case MusicService.STATUS_PAUSED:
                        sendBroadcastOnCommand(MusicService.COMMAND_RESUME);
                        break;
                    case MusicService.COMMAND_STOP:
                        sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
                    default:
                        break;
                }
            }
        });



//模式
        model3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String[] mode = new String[]{"顺序播放", "单曲播放", "列表播放", "随机播放"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MyMusic.this);
                builder.setTitle("播放模式");
                //setSingleChoiceItems方法为对话框设置了一个单选项选择列表，第一个参数为列表要显示的数据，第二个参数为被选择了的那个选项
                builder.setSingleChoiceItems(mode, playmode,//设置单选项，这里第二个参数是默认选择的序号，这里根据playmode的值来确定
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                playmode = arg1;
                            }
                        });
                builder.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                switch (playmode) {
                                    case 0:
                                        playmode = MyMusic.MODE_LIST_SEQUENCE;
                                        Toast.makeText(getApplicationContext(), R.string.sequence, Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        playmode = MyMusic.MODE_SINGLE_CYCLE;
                                        Toast.makeText(getApplicationContext(), R.string.singlecycle, Toast.LENGTH_SHORT).show();
                                        break;
                                    case 2:
                                        playmode = MyMusic.MODE_LIST_CYCLE;
                                        Toast.makeText(getApplicationContext(), R.string.listcycle, Toast.LENGTH_SHORT).show();
                                        break;
                                    case 3:
                                        playmode = MyMusic.MODE_LIST_RANDOM;
                                        Toast.makeText(getApplicationContext(), "随机模式", Toast.LENGTH_SHORT).show();
                                        break;

                                    default:
                                        break;
                                }
                            }
                        });
                builder.create().show();
            }
        });
        imgBtn_Next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                switch (playmode) {
                    case MyMusic.MODE_LIST_RANDOM:
                        sendBroadcastOnCommand(MusicService.COMMAND_RANDOM);
                        break;
                    default:
                        sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
                }
            }
        });
        //点击列表时，获取当前歌曲的number
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                number = position;
                sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
            }
        });
        //注册监听器seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //手没按进度条的情况
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //音乐没有停止的情况
                if (status != MusicService.STATUS_STOPPED) {//修复音乐暂停时拖动进度条，松手后音乐从原来暂停的地方继续播放，而不是松开手进度条的那个地方播放的bug
                    time = seekBar.getProgress();
                    //更新文本
                    text_Current.setText(formatTime(time));
                    //发送广播给Musicservice,执行跳转
                    sendBroadcastOnCommand(MusicService.COMMAND_SEEK_TO);
                }
                //音乐停止的情况
                if (status == MusicService.STATUS_PLAYING) {
                    //发送广播给MusicService，执行跳转
                    sendBroadcastOnCommand(MusicService.COMMAND_SEEK_TO);
                    //用迭代器继续让进度条恢复移动
                    seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
                }

                if (status == MusicService.STATUS_PAUSED) {
                    sendBroadcastOnCommand(MusicService.COMMAND_SEEK_TO);
                    sendBroadcastOnCommand(MusicService.COMMAND_RESUME);
                    seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
                }

            }

            //用手拖动时触发，此时停止进度条每秒进一格的操作
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //进度条暂停移动,sendEmptyMessage()暂停进度条的移动
                seekBarHandler.sendEmptyMessage(PROGRESS_PAUSE);
            }

            //拖动进度条后，改变状态，有拖动就执行该函数，托动一下，可能会执行几百次该函数
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //progress表示了行的SeekBar进度位置，该值保存在time中，以免最新的的进度刻度丢失
            }
        });
    }

    /**
     * 初始化音乐列表对象
     */
    private void initMusicList() {
        musicArrayList = MusicList.getMusicList();
        //避免重复添加音乐
        if (musicArrayList.isEmpty()) {
            Cursor mMusicCursor = this.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.AudioColumns.TITLE);
            //标题
            int indexTitle = mMusicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE);
            //艺术家
            int indexArtist = mMusicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
            //总时长
            int indexTotalTime = mMusicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION);
            //路径
            int indexPath = mMusicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);

            /**通过mMusicCursor游标遍历数据库，并将Music类对象加载带ArrayList中*/
            for (mMusicCursor.moveToFirst(); !mMusicCursor.isAfterLast(); mMusicCursor
                    .moveToNext()) {
                String strTitle = mMusicCursor.getString(indexTitle);
                String strArtist = mMusicCursor.getString(indexArtist);
                String strTotoalTime = mMusicCursor.getString(indexTotalTime);
                String strPath = mMusicCursor.getString(indexPath);

                if (strArtist.equals("<unknown>"))
                    strArtist = "无艺术家";
                Music music = new Music(strTitle, strArtist, strPath, strTotoalTime);
                musicArrayList.add(music);
            }
        }
    }

    //设置Activity的主题，包括修改背景图片等等
    private void setTheme(String theme) {
        if ("透明".equals(theme)) {
            linearLayout1.setBackgroundResource(R.color.tm);
            main_volumeLayout.setBackgroundResource(R.color.tm);
        } else if ("红色".equals(theme)) {
            linearLayout1.setBackgroundResource(R.color.hong);
            main_volumeLayout.setBackgroundResource(R.color.hong);
        } else if ("橙色".equals(theme)) {
            linearLayout1.setBackgroundResource(R.color.cheng);
            main_volumeLayout.setBackgroundResource(R.color.cheng);
        } else if ("黄色".equals(theme)) {
            linearLayout1.setBackgroundResource(R.color.huang);
            main_volumeLayout.setBackgroundResource(R.color.huang);
        } else if ("绿色".equals(theme)) {
            linearLayout1.setBackgroundResource(R.color.lv);
            main_volumeLayout.setBackgroundResource(R.color.lv);
        } else if ("青色".equals(theme)) {
            linearLayout1.setBackgroundResource(R.color.qing);
            main_volumeLayout.setBackgroundResource(R.color.qing);
        } else if ("模糊".equals(theme)) {
            root_Layout.setBackgroundResource(R.drawable.bg2);
            linearLayout1.setBackgroundResource(R.color.tm);
            main_volumeLayout.setBackgroundResource(R.color.tm);
        } else if ("彩色".equals(theme)) {
            root_Layout.setBackgroundResource(R.drawable.bg1);
            linearLayout1.setBackgroundResource(R.color.tm);
            main_volumeLayout.setBackgroundResource(R.color.tm);
        }
    }


    /*创建菜单*/
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /*处理菜单点击事件
    * onOptionsItemSelected()方法处理Menu项目被点击的事件。利用item.getItemId()识别被点击项目的编号，来执行相应的操作。
    * Resources.getStringArray()方法将读取array.xml中定义的数组，并返回一个字符串数组。*/
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_theme:
                new AlertDialog.Builder(this).setTitle("请选择主题").setItems(R.array.theme, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String theme = PropertyBean.THEMES[which];
                        MyMusic.this.setTheme(theme);
                        PropertyBean property = new PropertyBean(MyMusic.this);
                        property.setAndSaveTheme(theme);
                    }
                }).show();
                break;
            case R.id.menu_about:
                new AlertDialog.Builder(MyMusic.this)
                        .setTitle("GRacePlayer")
                        .setMessage(R.string.about2).show();
                break;
            case R.id.menu_quit:
                //退出程序
                new AlertDialog.Builder(this)
                        .setTitle("提示").
                        setMessage(R.string.quit).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        System.exit(0);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                }).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 设置适配器并初始化listView
     */
    private void initListView() {
        List<Map<String, String>> list_map = new ArrayList<Map<String, String>>();
        HashMap<String, String> map;
        SimpleAdapter simpleAdapter;
        for (Music music : musicArrayList) {
            map = new HashMap<String, String>();
            map.put("musicName", music.getmusicName());
            map.put("musicArtist", music.getmusicArtist());
            list_map.add(map);
        }
        String[] from = new String[]{"musicName", "musicArtist"};
        int[] to = {R.id.listview_tv_title_item, R.id.listview_tv_artist_item};
        simpleAdapter = new SimpleAdapter(this, list_map, R.layout.listview, from, to);
        list.setAdapter(simpleAdapter);
    }

    /**
     * 如果列表没有歌曲，则播放按钮不可用，并提醒用户
     */
    private void checkMusicfile() {
        if (musicArrayList.isEmpty()) {
            imgBtn_Next.setEnabled(false);
            imgBtn_PlayOrPause.setEnabled(false);
            imgBtn_Previous.setEnabled(false);
            // imgBtn_Stop.setEnabled(false);
            Toast.makeText(getApplicationContext(), "当前没有歌曲文件", Toast.LENGTH_SHORT).show();
        } else {
            imgBtn_Next.setEnabled(true);
            imgBtn_PlayOrPause.setEnabled(true);
            imgBtn_Previous.setEnabled(true);
            // imgBtn_Stop.setEnabled(true);
        }
    }

    /*
     当主界面被完全覆盖并重新载入时，将会调用onResume方法，而onCreate方法只在Activity第一次创建时调用，
     在这里，当音乐播放器后台播放，再次进入界面是，需要实时更新音量和主题的信息---------------------重要
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //状态改变，
        sendBroadcastOnCommand(MusicService.COMMAND_CHECK_IS_PLAYING);
        PropertyBean propetry = new PropertyBean(MyMusic.this);
        String theme = propetry.getTheme();
        //设置activity的主题
        setTheme(theme);//设置主题，使其后台之后再回来没有被重置
        audio_Control();
    }

    //时间除以60返回分秒，毫秒转化为以00:00为单位
    private String formatTime(int msec) {
        int minute = msec / 1000 / 60;
        int second = msec / 1000 % 60;
        String minuteString;
        String secondString;
        if (minute < 10) {
            minuteString = "0" + minute;
        } else {
            minuteString = "" + minute;
        }
        if (second < 10) {
            secondString = "0" + second;
        } else {
            secondString = "" + second;
        }
        return minuteString + ":" + secondString;
    }

    /**
     * 按命令发送广播，控制音乐播放。参数定义在MusicService类中
     */
    private void sendBroadcastOnCommand(int command) {
        Intent intent = new Intent(MusicService.BROADCAST_MUSICSERVICE_CONTROL);
        intent.putExtra("command", command);
        // 根据不同命令，封装不同的数据
        switch (command) {
            case MusicService.COMMAND_PLAY:
                intent.putExtra("number", number);
                break;
            case MusicService.COMMAND_SEEK_TO:
                intent.putExtra("time", time);
                break;
            case MusicService.COMMAND_PREVIOUS:
            case MusicService.COMMAND_NEXT:
            case MusicService.COMMAND_PAUSE:
            case MusicService.COMMAND_STOP:
            case MusicService.COMMAND_RESUME:
            default:
                break;
        }
        sendBroadcast(intent);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("MyMusic Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    //-------------------------继承OnGestureListener自动生成或需要重写的的方法

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return gestureDetector.onTouchEvent(event);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        if (arg1.getX() - arg0.getX() > 150) {
            Log.e("有滑动", "!!!!!!!!!");
            if (status == MusicService.STATUS_PLAYING)
                a1 = 1;
             else
                a1 = 0;
            Intent intent8 = new Intent(MyMusic.this, disc.class);

            startActivity(intent8);
            overridePendingTransition(R.anim.to_right_enter, R.anim.to_right_exit);
            Toast.makeText(getApplicationContext(), "唱片", Toast.LENGTH_SHORT).show();
            //finish();
        }
        return false;
    }

    public static int a2() {//获取播放状态到转转
        return a1;
    }
    public static String getmusicName1 () {//获取播放状态到转转
        String musicName1 = musicName;
        return musicName1 ;
    }
    public static String getmusicArtist1 () {//获取播放状态到转转
        String musicArtist1 = musicArtist;
        return musicArtist1 ;
    }



    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

//----------------------

    /**
     * 内部类，用于播放器状态更新的接收广播
     */
    class StatusChangedReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            musicName = intent.getStringExtra("musicName");
            musicArtist = intent.getStringExtra("musicArtist");

            MyMusic.this.textView.setText(musicName);
            MyMusic.this.textView5.setText(musicArtist);

            // 获取播放器状态
            status = intent.getIntExtra("status", -1);
            switch (status) {
                case MusicService.STATUS_PLAYING:
                    seekBarHandler.removeMessages(PROGRESS_INCREASE);
                    time = intent.getIntExtra("time", 0);
                    duration = intent.getIntExtra("duration", 0);
                    number = intent.getIntExtra("number", number);
                    list.setSelection(number);
                    seekBar.setProgress(time);
                    seekBar.setMax(duration);
                    seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
                    text_Duration.setText(formatTime(duration));
                    imgBtn_PlayOrPause.setBackgroundResource(R.drawable.pause);
                    //设置textview文字，提示已经播放的歌曲
                    MyMusic.this.setTitle(musicName + "-" + musicArtist);
                    //设置textview文字，提示已经播放的歌曲
                    MyMusic.this.textView.setText(musicName);
                    MyMusic.this.textView5.setText(musicArtist);
                    break;
                case MusicService.STATUS_PAUSED:
                    seekBarHandler.sendEmptyMessage(PROGRESS_PAUSE);
                    imgBtn_PlayOrPause.setBackgroundResource(R.drawable.play);
                    //MyMusic.this.textView.setText("暂停...");
                    MyMusic.this.setTitle("暂停...");
                    break;
                case MusicService.STATUS_STOPPED:
                    time = 0;
                    duration = 0;
                    text_Current.setText(formatTime(time));
                    text_Duration.setText(formatTime(duration));
                    seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
                    //MyMusic.this.textView.setText("停止...");
                    MyMusic.this.setTitle("停止...");
                    imgBtn_PlayOrPause.setBackgroundResource(R.drawable.play);
                    break;
                case MusicService.STATUS_COMPLETED:
                    //MyMusic.this.textView.setText("准备下一首....");
                    MyMusic.this.setTitle("准备下一首...");
                    /*
                    * getMusicList().size()-1是获取到当前所有歌的数量（从0开始的），那么当当前number等于这个值时间，意味播完了。 */
                    number = intent.getIntExtra("number", 0);
                    //判断播放模式
                    switch (playmode) {
                        case MODE_LIST_SEQUENCE://顺序播放
                            if (number == MusicList.getMusicList().size() - 1)
                                sendBroadcastOnCommand(MusicService.STATUS_STOPPED);
                            else
                                sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
                            break;
                        case MODE_SINGLE_CYCLE://当前播放
                            sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
                            break;
                        case MODE_LIST_CYCLE://列表播放
                            if (number == MusicList.getMusicList().size() - 1) {
                                number = 0;
                                sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
                            } else {
                                sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
                            }
                            break;
                        case MODE_LIST_RANDOM://随机播放
                            sendBroadcastOnCommand(MusicService.COMMAND_RANDOM);
                            break;
                        default:
                            break;
                    }
                    seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
                    imgBtn_PlayOrPause.setBackgroundResource(R.drawable.play);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (status == MusicService.STATUS_STOPPED) {
            stopService(new Intent(this, MusicService.class));
        }
        super.onDestroy();
    }

    // 媒体播放类
    private MediaPlayer player = new MediaPlayer();


    /**
     * 读取音乐文件
     */
    private void load(int number) {
        try {
            player.reset();
            player.setDataSource(MusicList.getMusicList().get(number).getmusicPath());
            player.prepare();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 播放音乐
     */
    private void play(int number) {
        // 停止当前播放
        if (player != null && player.isPlaying()) {
            player.stop();
        }
        load(number);
        player.start();
    }


    //用handler更新进度条三个状态
    private void initSeekBarHandler() {
        seekBarHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    //进度条往前走
                    case PROGRESS_INCREASE:
                        if (seekBar.getProgress() < duration) {//duration总长度
                            //进度条前进一秒
                            seekBar.setProgress(time);//修复返回重新打开进度条归零的bug，重新获取当前的time
                            // seekBar.incrementProgressBy(1000); 修复暂停后前进2s 的BUG

                            //PROGRESS_INCREASE没执行一遍，进度条都前进1000毫秒，
                            // 迭代器操作：sendEmptyMessageDelayed( run , 1000);每1000毫秒执行一遍run
                            seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
                            //修改显示当前进度的文本
                            text_Current.setText(formatTime(time));
                            time += 1000;
                        }
                        break;
                    //进度条暂停
                    case PROGRESS_PAUSE:
                        //用removeMessages()将PROGRESS_INCREASE消息移走，进度条就会停止推进
                        seekBarHandler.removeMessages(PROGRESS_INCREASE);
                        break;
                    //重置进度条进度
                    case PROGRESS_RESET:
                        seekBarHandler.removeMessages(PROGRESS_INCREASE);
                        seekBar.setProgress(0);
                        text_Current.setText("00:00");
                        break;
                }
            }
        };
    }


    //y手机按键按下时触发
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int progress;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK://按下返回键
                exitByDoubleClick();
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN://按下音量键下键执行代码
                progress = seekbar_vol.getProgress();
                if (progress != 0) {
                    seekbar_vol.setProgress(progress - 1);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP://按下音量键上键执行代码
                progress = seekbar_vol.getProgress();
                if (progress != seekbar_vol.getMax()) {
                    seekbar_vol.setProgress(progress + 1);
                }
                return true;
            default:
                break;
        }
        return false;
    }

    //按下返回键执行代码
    private void exitByDoubleClick() {
        Timer timer = null;
        if (isExit == false) {
            isExit = true;
            Toast.makeText(this, "再按一次退出程序！！！", Toast.LENGTH_SHORT).show();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);//2秒后执行tt任务
        } else {
            finish();
            System.exit(0);
        }
    }

    //音量控制函数
    private void audio_Control() {
        //获取音量管理器
        final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        //设置当前调整音量大小只是针对媒体音乐
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //设置滑动条最大值
        final int max_progress = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekbar_vol.setMax(max_progress);
        //获取当前音量
        int progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekbar_vol.setProgress(progress);
        tv_vol.setText("音量： " + (progress * 100 / max_progress) + "%");
        seekbar_vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                tv_vol.setText("音量： " + (arg1 * 100) / (max_progress) + "%");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, AudioManager.FLAG_PLAY_SOUND);
            }
        });
    }
}
