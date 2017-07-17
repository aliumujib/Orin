package com.alium.orin.ui.fragments.mainactivity.library.pager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.alium.orin.adapter.album.AlbumAdapter;
import com.alium.orin.loader.AlbumLoader;
import com.alium.orin.model.Album;
import com.alium.orin.util.PreferenceUtil;
import com.alium.orin.R;
import com.alium.orin.interfaces.LoaderIds;
import com.alium.orin.misc.WrappedAsyncTaskLoader;
import com.alium.orin.util.Util;
import com.alium.orin.util.ViewUtil;
import com.alium.orin.views.GridSpacingItemDecoration;
import com.alium.orin.views.IndexLayoutManager;
import com.github.captain_miao.optroundcardview.OptRoundCardView;
import com.kabouzeid.appthemehelper.util.ATHUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<AlbumAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Album>> {
    public static final String TAG = AlbumsFragment.class.getSimpleName();
    private static final int LOADER_ID = LoaderIds.ALBUMS_FRAGMENT;

    @Nullable
    @BindView(R.id.index_layout)
    IndexLayoutManager indexLayoutManager;

    @Nullable
    @BindView(R.id.card_view)
    OptRoundCardView optRoundCardView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected AlbumAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        ArrayList<Album> dataSet = getAdapter() == null ? new ArrayList<Album>() : getAdapter().getDataSet();
        return new AlbumAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes,
                loadUsePalette(),
                getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_albums;
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance(getActivity()).albumColoredFooters();
    }

    @Override
    protected void setUsePalette(boolean usePalette) {
        getAdapter().usePalette(usePalette);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();

    }


    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumGridSize(getActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setAlbumGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumGridSizeLand(getActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setAlbumGridSizeLand(gridSize);
    }

    @Override
    protected void saveUsePalette(boolean usePalette) {
        PreferenceUtil.getInstance(getActivity()).setAlbumColoredFooters(usePalette);
    }

    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<ArrayList<Album>> onCreateLoader(int id, Bundle args) {
        return new AsyncAlbumLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Album>> loader, ArrayList<Album> data) {
        sortAlbums(data);
        getAdapter().swapDataSet(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Album>> loader) {
        getAdapter().swapDataSet(new ArrayList<Album>());
    }

    private void sortAlbums(ArrayList<Album> data) {
        Collections.sort(data, new Comparator<Album>() {
            @Override
            public int compare(Album lhs, Album rhs) {
                return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!Util.isTablet(getResources())) {
            ViewUtil.setMargins(recyclerView, 0, 32, 0, 0);
            final int spanCount = loadGridSize(); // no of columns
            int spacing = 4; // 4px

            // Create a custom SpanSizeLookup where the first item spans both columns
            //I wonder why this has to be juxtaposed for it to work
            getLayoutManager().setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (true) {
                        return spanCount;
                    } else {
                        return 1;
                    }
                }
            });

            indexLayoutManager.attach(getRecyclerView(), getActivity());
            optRoundCardView.setCardBackgroundColor(ATHUtil.resolveColor(getActivity(), R.attr.cardBackgroundColor));
        }else {
            GridSpacingItemDecoration gridSpacingItemDecoration = new GridSpacingItemDecoration(loadGridSize(), 4, false);
            getRecyclerView().addItemDecoration(gridSpacingItemDecoration);
        }
    }

    @Override
    protected int getItemLayoutRes() {
        if(!Util.isTablet(getResources())){
            return R.layout.item_sectioned_album;
        }else {
            return super.getItemLayoutRes();
        }
    }

    @LayoutRes
    protected int getLayoutRes() {
        super.getLayoutRes();


        if (!Util.isTablet(getResources())) {
            return R.layout.fragment_albums_rv;
        } else {
            return super.getLayoutRes();
        }
    }

    private static class AsyncAlbumLoader extends WrappedAsyncTaskLoader<ArrayList<Album>> {
        public AsyncAlbumLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Album> loadInBackground() {
            return AlbumLoader.getAllAlbums(getContext());
        }
    }
}
