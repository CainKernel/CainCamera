package com.cgfay.caincamera.activity.videoedit;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.adapter.MusicAdapter;
import com.cgfay.caincamera.bean.Music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicSelectActivity extends AppCompatActivity {

    public static final int RESULT_MUSIC = 0x100;

    private ListView mListView;
    private MusicAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_select);
        mListView = (ListView) findViewById(R.id.music_list);
        new MusicSelectTask().execute();
    }

    private class MusicSelectTask extends AsyncTask<Void, Void, List<Music>> implements AdapterView.OnItemClickListener {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Music> doInBackground(Void... voids) {
            List<Music> musics = new ArrayList<>();
            Cursor cursor = getApplicationContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                    MediaStore.Audio.Media.DATA + " like ?",
                    new String[]{Environment.getExternalStorageDirectory() + File.separator + "%"},
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
                    if (isMusic != null && isMusic.equals("")) continue;
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (!path.endsWith(".mp3") || duration<60 * 1000) {
                        continue;
                    }
                    Music music = new Music();
                    music.setDuration(duration);
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    music.setId(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                    music.setName(title);
                    music.setSingerName(artist);
                    music.setSongUrl(path);
                    musics.add(music);
                }
                cursor.close();
            }
            return musics;
        }

        @Override
        protected void onPostExecute(List<Music> musics) {
            super.onPostExecute(musics);
            mAdapter = new MusicAdapter(MusicSelectActivity.this, musics);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Music music = mAdapter.getItem(i);
            Intent intent = new Intent();
            intent.putExtra("music", music.getSongUrl());
            setResult(RESULT_MUSIC, intent);
            finish();
        }
    }
}
