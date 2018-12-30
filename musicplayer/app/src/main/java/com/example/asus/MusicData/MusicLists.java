package com.example.asus.MusicData;

import java.util.ArrayList;

public class MusicLists {
    private static ArrayList<Music> musicarray = new ArrayList<Music>();
    private MusicLists(){}

    public static ArrayList<Music> getMusicList()
    {
        return musicarray;
    }
}
