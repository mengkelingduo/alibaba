package com.example.asus.mykougoumusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.asus.MusicData.MusicList;

import java.util.Random;

public class MusicService extends Service {
    // 播放控制命令，标识操作
    public static final int COMMAND_UNKNOWN = -1;
    public static final int COMMAND_PLAY = 0;
    public static final int COMMAND_PAUSE = 1;
    public static final int COMMAND_STOP = 2;
    public static final int COMMAND_RESUME = 3;
    public static final int COMMAND_PREVIOUS = 4;
    public static final int COMMAND_NEXT = 5;
    public static final int COMMAND_CHECK_IS_PLAYING = 6;
    public static final int COMMAND_SEEK_TO = 7;
    public static final int COMMAND_RANDOM = 8;
    public static final int COMMAND_SINGLE_CYCLE = 9;
    // 播放器状态
    public static final int STATUS_PLAYING = 0;
    public static final int STATUS_PAUSED = 1;
    public static final int STATUS_STOPPED = 2;
    public static final int STATUS_COMPLETED = 3;
    // 广播标识
    public static final String BROADCAST_MUSICSERVICE_CONTROL = "MusicService.ACTION_CONTROL";
    public static final String BROADCAST_MUSICSERVICE_UPDATE_STATUS = "MusicService.ACTION_UPDATE";


    //歌曲序号，从0开始
    private int number = 0;
    private int status;
    // 媒体播放类
    private MediaPlayer player = new MediaPlayer();
    //状态栏
    private NotificationManager manager;

    // 广播接收器
    private CommandReceiver receiver;
    private boolean phone = false;

    @Override

    public void onCreate() {
        super.onCreate();
        // 绑定广播接收器，可以接收广播
        bindCommandReceiver();
        status = MusicService.STATUS_STOPPED;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }
    //状态栏
    public void updateNotification() {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.statusbar);
        views.setImageViewResource(R.id.icon, R.drawable.ic_allapps_pressed);
        views.setTextViewText(R.id.musicName, MusicList.getMusicList().get(number).getmusicName());
        views.setTextViewText(R.id.artist, MusicList.getMusicList().get(number).getmusicArtist());

        if (status == MusicService.STATUS_PLAYING) {
            views.setViewVisibility(R.id.play_play, View.GONE);
            views.setViewVisibility(R.id.play_pause, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.play_play, View.VISIBLE);
            views.setViewVisibility(R.id.play_pause, View.GONE);
        }
        views.setOnClickPendingIntent(R.id.play_previous, pre_PendingIntent());
        views.setOnClickPendingIntent(R.id.play_play, play_PendingIntent());
        views.setOnClickPendingIntent(R.id.play_pause, pause_PendingIntent());
        views.setOnClickPendingIntent(R.id.play_next, next_PendingIntent());
        views.setOnClickPendingIntent(R.id.close, close_PendingIntent());

        Intent intent = new Intent(this, MyMusic.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContent(views)
                .setContentIntent(pi)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ap_icon_on);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        manager.notify(0x3131, notification);
    }

    private PendingIntent pre_PendingIntent() {
        Intent intent = new Intent("PREVIOUS");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        return pendingIntent;
    }

    private PendingIntent close_PendingIntent() {
        Intent intent = new Intent("CLOSE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        return pendingIntent;
    }

    private PendingIntent next_PendingIntent() {
        Intent intent = new Intent("NEXT");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        return pendingIntent;
    }

    private PendingIntent pause_PendingIntent() {
        Intent intent = new Intent("PAUSE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        return pendingIntent;
    }

    private PendingIntent play_PendingIntent() {
        Intent intent = new Intent("RESUME");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        return pendingIntent;
    }
    //-----------------------------状态栏

    private final class MyPhoneListener extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (status == MusicService.STATUS_PLAYING) {
                        pause();
                        phone = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (phone == true) {
                        resume();
                        phone = false;
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        sendBroadcastOnStatusChanged(MusicService.STATUS_STOPPED);
        manager.cancel(0x3131);
        // 释放播放器资源
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 绑定广播接收器
     */
    private void bindCommandReceiver() {
        receiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter(BROADCAST_MUSICSERVICE_CONTROL);

        filter.addAction(BROADCAST_MUSICSERVICE_CONTROL);
        filter.addAction("RESUME");
        filter.addAction("PAUSE");
        filter.addAction("PREVIOUS");
        filter.addAction("NEXT");
        filter.addAction("CLOSE");


        registerReceiver(receiver, filter);
    }

    /**
     * 内部类，接收广播命令，并执行操作
     */
    class CommandReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String ctrl_code = intent.getAction();

            if (BROADCAST_MUSICSERVICE_CONTROL.equals(ctrl_code)) {
                // 获取命令
                int command = intent.getIntExtra("command", COMMAND_UNKNOWN);
                // 执行命令
                switch (command) {
                    //执行跳转代码,更新进度条
                    case COMMAND_SEEK_TO:
                        seekTo(intent.getIntExtra("time", 0));
                        break;
                    case COMMAND_PLAY:
                        number = intent.getIntExtra("number", 0);
                        play(number);
                        break;
                    case COMMAND_PREVIOUS:
                        moveNumberToPrevious();
                        break;
                    case COMMAND_NEXT:
                        moveNumberToNext();
                        break;
                    case COMMAND_PAUSE:
                        pause();
                        break;
                    case COMMAND_STOP:
                        stop();
                        break;
                    case COMMAND_RESUME:
                        resume();
                        break;
                    //状态改变
                    case COMMAND_CHECK_IS_PLAYING:
                        if (player != null && player.isPlaying()) {
                            sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
                        }
                        break;
                    //执行随机播放
                    case COMMAND_RANDOM:
                        Random rand = new Random();
                        int i;
                        do {
                            i = rand.nextInt(MusicList.getMusicList().size() - 1);
                        } while (i == number);
                        number = i;
                        play(number);
                        break;

                    case COMMAND_UNKNOWN:
                    default:
                        break;
                }
            } else if ("RESUME".equals(ctrl_code)) {
                resume();
            } else if ("PAUSE".equals(ctrl_code)) {
                pause();
            } else if ("PREVIOUS".equals(ctrl_code)) {
                moveNumberToPrevious();
            } else if ("NEXT".equals(ctrl_code)) {
                moveNumberToNext();
            } else if ("CLOSE".equals(ctrl_code)) {
                manager.cancel(0x3131);
                System.exit(0);
            }
        }

    }

    /**
     * 发送广播，提醒状态改变了
     */
    //获取当前状态
    private void sendBroadcastOnStatusChanged(int status) {
        Intent intent = new Intent(BROADCAST_MUSICSERVICE_UPDATE_STATUS);
        intent.putExtra("status", status);
        if (status != STATUS_STOPPED) {
            intent.putExtra("time", player.getCurrentPosition());//获取当前位置时间
            intent.putExtra("duration", player.getDuration());//获取该歌曲全部时间长度
            intent.putExtra("number", number);
            intent.putExtra("musicName", MusicList.getMusicList().get(number).getmusicName());
            intent.putExtra("musicArtist", MusicList.getMusicList().get(number).getmusicArtist());
        }
        sendBroadcast(intent);
    }

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
        // 注册监听器
        player.setOnCompletionListener(completionListener);
    }

    // 播放结束监听器
    OnCompletionListener completionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer player) {
            if (player.isLooping()) {
                replay();
            } else {
                sendBroadcastOnStatusChanged(MusicService.STATUS_COMPLETED);
            }
        }
    };

    /**
     * 选择下一曲
     */
    private void moveNumberToNext() {

        // 判断是否到达了列表底端
        if ((number) == MusicList.getMusicList().size() - 1) {
            Toast.makeText(MusicService.this, "已经到达列表底部！", Toast.LENGTH_SHORT).show();
        } else {
            ++number;
            play(number);
        }
    }

    /**
     * 选择s上一曲
     */
    private void moveNumberToPrevious() {

        // 判断是否到达了列表顶端
        if (number == 0) {
            Toast.makeText(MusicService.this, "已经到达列表顶部", Toast.LENGTH_SHORT).show();
        } else {
            --number;
            play(number);
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
        status = MusicService.STATUS_PLAYING;
        sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
        updateNotification();//更新状态栏
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        if (player.isPlaying()) {
            player.pause();
            status = MusicService.STATUS_PAUSED;
            sendBroadcastOnStatusChanged(MusicService.STATUS_PAUSED);
            updateNotification();//更新状态栏
        }
    }

    /**
     * 停止播放
     */
    private void stop() {
        if (status != MusicService.STATUS_STOPPED) {
            player.stop();
            status = MusicService.STATUS_STOPPED;
            sendBroadcastOnStatusChanged(MusicService.STATUS_STOPPED);
            updateNotification();//更新状态栏
        }
    }

    /**
     * 恢复播放（暂停之后）
     */
    private void resume() {
        player.start();
        status = MusicService.STATUS_PLAYING;
        sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
        updateNotification();//更新状态栏
    }

    /**
     * 重新播放（播放完成之后）
     */
    private void replay() {
        player.start();
        status = MusicService.STATUS_PLAYING;
        sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
        updateNotification();//更新状态栏
    }

    //处理MediaPlay为null的情况
    private void seekTo(int time) {
        player.seekTo(time);
        status = MusicService.STATUS_PLAYING;
        sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
        updateNotification();//更新状态栏
    }

}
