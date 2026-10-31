package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.Collections;
import java.util.List;

/**
 * Listens for system media sessions and notifications across Spotify, Bitchord,
 * YouTube, and any background audio apps to power the Samsung One UI 9 Now Bar.
 */
public class MediaNotificationListener extends NotificationListenerService {

    public interface Callback {
        void onMediaSessionsChanged();
    }

    private static volatile MediaNotificationListener instance;
    private static Callback callback;

    public static MediaNotificationListener getInstance() {
        return instance;
    }

    public static void setCallback(Callback cb) {
        callback = cb;
        if (cb != null) {
            cb.onMediaSessionsChanged();
        }
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        instance = this;
        if (callback != null) {
            callback.onMediaSessionsChanged();
        }
    }

    @Override
    public void onListenerDisconnected() {
        instance = null;
        super.onListenerDisconnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (callback != null) {
            callback.onMediaSessionsChanged();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        if (callback != null) {
            callback.onMediaSessionsChanged();
        }
    }

    public static List<MediaController> getActiveSessions(Context context) {
        try {
            MediaSessionManager msm = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
            if (msm != null) {
                try {
                    List<MediaController> sessions = msm.getActiveSessions(null);
                    if (sessions != null && !sessions.isEmpty()) {
                        return sessions;
                    }
                } catch (Exception ignored) {}

                try {
                    List<MediaController> sessions = msm.getActiveSessions(new ComponentName(context, MediaNotificationListener.class));
                    if (sessions != null && !sessions.isEmpty()) {
                        return sessions;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }
}
