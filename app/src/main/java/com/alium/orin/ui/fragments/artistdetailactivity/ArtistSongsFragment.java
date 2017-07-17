package com.alium.orin.ui.fragments.artistdetailactivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alium.orin.R;

/**
 * Created by abdulmujibaliu on 7/11/17.
 */

public class ArtistSongsFragment extends Fragment {


    public ArtistSongsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist_album_song_list, container, false);
    }

}
