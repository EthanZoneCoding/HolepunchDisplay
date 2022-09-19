package com.ethanzone.hpdisplay;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.util.Log;
import android.view.KeyEvent;

public class MediaManager {

    public AudioManager audioManager;
    private final MediaSessionManager mediaSessionManager;
    private MediaController mediaController;
    private final ComponentName notificationListener;
    private final Context context;

    public MediaManager(Context context) {
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mediaSessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        this.notificationListener = new ComponentName(context, NotiService.class);
        this.context = context;
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    public void updateMedia(UIState uiState) {
        if (checkMedia() && uiState.miniIcon == null) {
            try {
                uiState.title = getTitle();
                uiState.description = getDescription();
                if (audioManager.isMusicActive()) {
                    uiState.icon = getIcon();
                } else {
                    uiState.icon = this.context.getDrawable(R.drawable.playbutton);
                }
                uiState.miniIconRight = getIcon();
            } catch (NullPointerException e) {
                e.printStackTrace();
                uiState = ((HPDisplay) this.context).notificationHandler.backedUpState;
            }
            uiState.shape = UIState.SHAPE_NOCHANGE;
            uiState.apply(this.context);
        }
    }

    public boolean checkMedia() {
        try {
            if (mediaSessionManager.getActiveSessions(notificationListener).size() > 0) {
                mediaController = mediaSessionManager.getActiveSessions(notificationListener).get(0);
                return true;
            }
        } catch (SecurityException e) {
            // Permissions not correctly set
        }
        return false;
    }

    public Drawable getIcon() {
        return new BitmapDrawable(context.getResources(), mediaController.getMetadata().getDescription().getIconBitmap());
    }

    public String getTitle() {
        return this.mediaController.getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE);
    }

    public String getDescription() {
        String album = this.mediaController.getMetadata().getString(MediaMetadata.METADATA_KEY_ALBUM);
        String artist = this.mediaController.getMetadata().getString(MediaMetadata.METADATA_KEY_ARTIST);
        if (album != null && artist != null) {
            return album + " - " + artist;
        } else if (album != null) {
            return album;
        } else if (artist != null) {
            return artist;
        } else {
            return "";
        }
    }

    public void pause() {
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
    }

    public void play() {
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
        audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
    }
}
