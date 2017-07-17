package com.alium.orin.ui.fragments.artistdetailactivity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alium.orin.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistAlbumsFragment extends Fragment {


    public ArtistAlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist_album_song_list, container, false);
    }

}
