package com.alium.orin.adapter.album;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alium.orin.R;
import com.alium.orin.glide.SongGlideRequest;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.interfaces.MusicServiceEventListener;
import com.alium.orin.model.Album;
import com.alium.orin.model.Song;
import com.alium.orin.ui.activities.MainActivity;
import com.alium.orin.ui.activities.base.AbsMusicServiceActivity;
import com.alium.orin.util.MusicUtil;
import com.alium.orin.util.NavigationUtil;
import com.alium.orin.util.PhonographColorUtil;
import com.alium.orin.util.Util;
import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.alium.orin.adapter.base.AbsMultiSelectAdapter;
import com.alium.orin.adapter.base.MediaEntryViewHolder;
import com.alium.orin.glide.PhonographColoredTarget;
import com.alium.orin.helper.menu.SongsMenuHelper;
import com.alium.orin.interfaces.CabHolder;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.alium.orin.util.PhonographColorUtil.CIRCLE_TRANSPARENCY;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumAdapter extends AbsMultiSelectAdapter<AlbumAdapter.ViewHolder, Album> implements FastScrollRecyclerView.SectionedAdapter, MusicServiceEventListener {

    public static final String TAG = AlbumAdapter.class.getSimpleName();

    protected final AppCompatActivity activity;
    protected ArrayList<Album> dataSet;
    private LinkedHashMap<String, Integer> mMapIndex;
    private ArrayList<String> mSectionList;
    private String[] mSections;
    protected int itemLayoutRes;

    protected boolean usePalette = false;

    public AlbumAdapter(@NonNull AppCompatActivity activity, ArrayList<Album> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;
        ((AbsMusicServiceActivity) activity).addMusicServiceEventListener(this);
        setHasStableIds(true);
    }


    private String getSection(String albumTitle) {
        Character character = albumTitle.toCharArray()[0];
        if (Character.isDigit(character)) {
            return "#";
        } else {
            return character.toString();
        }
    }

    private void fillSections() {
        mMapIndex = new LinkedHashMap<>();

        for (int x = 0; x < dataSet.size(); x++) {
            String fruit = dataSet.get(x).getTitle();
            if (fruit.length() > 1) {
                String ch = getSection(dataSet.get(x).getTitle());

                ch = ch.toUpperCase();
                if (!mMapIndex.containsKey(ch)) {
                    mMapIndex.put(ch, x);
                    Log.d(TAG, "" + ch + " INDEX " + x);
                }
            }
        }
        Set<String> sectionLetters = mMapIndex.keySet();
        // create a list from the set to sort
        mSectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(mSectionList);

        mSections = new String[mSectionList.size()];
        mSectionList.toArray(mSections);

    }


    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    public void swapDataSet(ArrayList<Album> dataSet) {
        this.dataSet = dataSet;
        fillSections();
        notifyDataSetChanged();
    }

    public ArrayList<Album> getDataSet() {
        return dataSet;
    }

    @Override
    public int getItemViewType(int position) {
        return MAIN_LIST_ALBUM_ITEM;
    }

    protected int TOP_ALBUM_ITEM = 1;
    protected int MAIN_LIST_ALBUM_ITEM = 2;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (activity instanceof MainActivity && viewType == TOP_ALBUM_ITEM) {
            view = LayoutInflater.from(activity).inflate(R.layout.item_grid_full_width, parent, false);
        } else {
            view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        }
        return createViewHolder(view, viewType);
    }

    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view);
    }

    protected String getAlbumTitle(Album album) {
        return album.getTitle();
    }

    protected String getAlbumText(Album album) {
        return album.getArtistName();
    }

    protected String getExtraDetailsWithDot(Album album) {
        return Util.getExtraDetailsWithDot(activity, album);
    }

    protected String getExtraDetailsLineSpace(Album album) {
        return Util.getExtraDetailsLineSpace(activity, album);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        final boolean isChecked = isChecked(album);
        holder.itemView.setActivated(isChecked);


        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        if (holder.title != null) {
            holder.title.setText(getAlbumTitle(album));
        }
        if (holder.text != null) {
            holder.text.setText(getAlbumText(album));
        }
        if (holder.text2 != null) {
            holder.text2.setText(getExtraDetailsWithDot(album));

        }

        if (holder.playPauseBtn != null) {
            if (MusicPlayerRemote.isPlaying()) {
                updatePlayPauseBtn(holder.playPauseBtn, album);
            } else {
                holder.playPauseBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            }
        }

        loadAlbumCover(album, holder);

        //SETUP CIRCULAR LABEL
        String section = getSection(album.getTitle());

        if (holder.topCircularText != null && holder.bottomCircularText != null) {

            Drawable drawable = activity.getResources().getDrawable(R.drawable.circle_white_bg);
            drawable.setColorFilter(PhonographColorUtil.setTransparency(ThemeStore.primaryColor(activity), CIRCLE_TRANSPARENCY), PorterDuff.Mode.SRC_IN);
            holder.topCircularText.setBackground(drawable);

            holder.topCircularText.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(ThemeStore.primaryColor(activity))));

            holder.topCircularText.setText(section);
            holder.bottomCircularText.setText(section);

            if (mMapIndex.get(section) != null) {
                if (mMapIndex.get(section) == position) {
                    holder.topCircularText.setVisibility(View.VISIBLE);
                } else {
                    holder.topCircularText.setVisibility(View.INVISIBLE);
                }
            }
        }


    }

    private void updatePlayPauseBtn(@NonNull ImageView playPauseBtn, Album album) {
        if (isMusicPlaying()) {
            if (isCurrentAlbum(album.getId())) {
                playPauseBtn.setImageResource(R.drawable.ic_pause_white_24dp);
            } else {
                playPauseBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            }
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    private boolean isCurrentAlbum(int albumId) {
        if (!(MusicPlayerRemote.getCurrentSong().albumId == albumId)) {
            return false;
        }
        return true;
    }

    private boolean isMusicPlaying() {
        if (!MusicPlayerRemote.isPlaying()) {
            return false;
        }
        return true;
    }

    protected void setColors(int color, ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
            if (holder.text2 != null) {
                holder.text2.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
        }
    }

    protected void loadAlbumCover(Album album, final ViewHolder holder) {
        if (holder.image == null) return;

        SongGlideRequest.Builder.from(Glide.with(activity), album.safeGetFirstSong())
                .checkIgnoreMediaStore(activity)
                .generatePalette(activity).build()
                .into(new PhonographColoredTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        //setColors(getDefaultFooterColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
//                        if (usePalette)
//                            setColors(color, holder);
//                        else
//                            setColors(getDefaultFooterColor(), holder);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).getId();
    }

    @Override
    protected Album getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Album album) {
        return album.getTitle();
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Album> selection) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
    }

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Album> albums) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Album album : albums) {
            songs.addAll(album.songs);
        }
        return songs;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return MusicUtil.getSectionName(dataSet.get(position).getTitle());
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {

    }

    @Override
    public void onPlayStateChanged() {
        int postion;
        if ((postion = getCurrentPlayingAlbumPosition(getCurrentPlayingAlbumPosition(MusicPlayerRemote.getCurrentSong().albumId))) != -20) {
            notifyItemChanged(postion);
        }

        Log.d(TAG, "CHANGED PLAY STATE" + postion);

    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {

    }

    //TODO .. implement binary search for larger datasets
    public int getCurrentPlayingAlbumPosition(int albumId) {
        Log.d(TAG, "GET POS FOR ALBUM" + albumId);
        int position = -20;
        for (int i = 0; i < dataSet.size(); i++) {
            if (dataSet.get(i).getId() == albumId) {
                Log.d(TAG, "ALBUM SEARCH QUERY " + albumId + " CURRENT LOOP POSTION " + i + " CURRENT LOOP POS ID " + dataSet.get(i).getId());
                position = i;
                return position;
            }
        }
        return position;
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            setImageTransitionName(activity.getString(R.string.transition_album_art));
            if (menu != null) {
                menu.setVisibility(View.GONE);
            }

            if (playPauseWrapper != null) {
                playPauseWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "WRAPPER CLICK");
                        handlePlaying();
                    }
                });
            }

            if (playPauseBtn != null) {
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "BUTTON CLICK");
                        handlePlaying();
                    }
                });
            }

        }

        private void handlePlaying() {
            Album album = dataSet.get(getAdapterPosition());

            //IF NO MUSIC HAS BEEN PLAYED


            //IF MUSIC IS PLAYING ALREADY
            if (isCurrentAlbum(album.getId())) {
                if (isMusicPlaying()) {
                    Log.d(TAG, "PAUSE HERE");
                    pausePlaying();
                } else {
                    Log.d(TAG, "PLAY HERE 1");
                    starPlayinging(album);
                }
            } else {
                Log.d(TAG, "PLAY HERE 2");
                pausePlaying();
                starPlayinging(album);
            }
            updatePlayPauseBtn(playPauseBtn, album);
        }

        private void pausePlaying() {
            MusicPlayerRemote.pauseSong();
            MusicPlayerRemote.clearQueue();
        }

        private void starPlayinging(Album album) {
            ArrayList<Album> albumList = new ArrayList<>();
            albumList.add(album);
            MusicPlayerRemote.clearQueue();
            MusicPlayerRemote.openQueue(getSongList(albumList), getAdapterPosition(), true);
        }


        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] albumPairs = new Pair[]{
                        Pair.create(image,
                                activity.getResources().getString(R.string.transition_album_art)
                        )};
                NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).getId(), albumPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
