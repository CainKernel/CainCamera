package com.cgfay.videolibrary.fragment;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cgfay.videolibrary.bean.Music;
import com.cgfay.videolibrary.R;
import com.cgfay.videolibrary.adapter.MusicSelectAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 音乐选择页面
 */
public class MusicSelectFragment extends Fragment {

    private ListView mMusicListView;
    private MusicSelectAdapter mMusicSelectAdapter;

    private MusicSelectTask mMusicSelectTask;

    private OnMusicSelectedListener mMusicSelectedListener;

    public MusicSelectFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_music_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mMusicListView = (ListView) view.findViewById(R.id.music_list);
        mMusicSelectTask = new MusicSelectTask();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMusicSelectTask != null) {
            mMusicSelectTask.execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMusicSelectTask != null) {
            mMusicSelectTask.cancel(true);
        }
    }

    private class MusicSelectTask extends AsyncTask<Void, Void, List<Music>> implements AdapterView.OnItemClickListener {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Music> doInBackground(Void... voids) {
            List<Music> musics = new ArrayList<>();
            Cursor cursor = getActivity().getContentResolver().query(
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
                    if (!path.endsWith(".mp3") || duration < 60 * 1000) {
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
            mMusicSelectAdapter = new MusicSelectAdapter(mMusicListView.getContext(), musics);
            mMusicListView.setAdapter(mMusicSelectAdapter);
            mMusicListView.setOnItemClickListener(this);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Music music = mMusicSelectAdapter.getItem(i);
            if (mMusicSelectedListener != null) {
                mMusicSelectedListener.onMusicSelected(music);
                mMusicSelectedListener = null;
            }
        }
    }

    public interface OnMusicSelectedListener {
        void onMusicSelected(Music music);
    }

    public void addOnMusicSelectedListener(OnMusicSelectedListener listener) {
        mMusicSelectedListener = listener;
    }
}
