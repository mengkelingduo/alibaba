package com.example.asus.MusicData;

/**
 * Created by asus on 2018/9/19.
 * Music类，包括歌曲名，艺术家，路径，时长等属性， 以及相关的获取方法dd
 */

public class Music {
    private String musicName;
    private String musicArtist;
    private String musicPath;
    private String musicDuration;

    public Music (String musicName,String musicArtist,String musicPath,String musicDuration)
    {
        this.musicName = musicName;
        this.musicArtist = musicArtist;
        this.musicPath = musicPath;
        this.musicDuration = musicDuration;
    }
    public String getmusicName()
    {
        return this.musicName;
    }
    public String getmusicArtist()
    {
        return this.musicArtist;
    }
    public String getmusicPath()
    {
        return this.musicPath;
    }
    public String getmusicDuration()
    {
        return this.musicDuration;
    }

}
