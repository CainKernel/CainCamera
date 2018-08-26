package com.cgfay.medialibrary.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cgfay.medialibrary.R;
import com.cgfay.medialibrary.model.MediaItem;
import com.cgfay.medialibrary.engine.MediaScanParam;
import com.cgfay.medialibrary.utils.MediaMetadataUtils;

public class PreviewFragment extends Fragment {

    private static final String CURRENT_MEDIA = "current_media";


    public static PreviewFragment newInstance(MediaItem item) {
        PreviewFragment fragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CURRENT_MEDIA, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MediaItem item = getArguments().getParcelable(CURRENT_MEDIA);
        if (item == null) {
            return;
        }

        ImageView videoPlay = view.findViewById(R.id.iv_play);
        if (item.isVideo()) {
            videoPlay.setVisibility(View.VISIBLE);
            videoPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } else {
            videoPlay.setVisibility(View.GONE);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);

        Point size = MediaMetadataUtils.getBitmapSize(item.getContentUri(), getActivity());
        if (item.isGif()) {
            MediaScanParam.getInstance().mediaLoader.loadGif(getContext(), size.x, size.y,
                    imageView, item.getContentUri());
        } else {
            MediaScanParam.getInstance().mediaLoader.loadImage(getContext(), size.x, size.y,
                    imageView, item.getContentUri());
        }

    }


}
