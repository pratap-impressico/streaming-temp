package screen.record.and.serve.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import screen.record.and.serve.R;
import screen.record.and.serve.server.AndroidWebServer;
import screen.record.and.serve.server.Recorder;

public class MyForegroundService extends Service {
  private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";

  public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

  public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

  public static final String ACTION_PAUSE = "ACTION_PAUSE";

  public static final String ACTION_PLAY = "ACTION_PLAY";
  private AndroidWebServer mAndroidWebServer;
  private Recorder mRecorder;

  private static final String NOTIFICATION_CHANNEL_ID =
      MyForegroundService.class.getCanonicalName();
  public MyForegroundService() {
  }

  @Override public void onCreate() {
    super.onCreate();
    Log.d(TAG_FOREGROUND_SERVICE, "My foreground service onCreate().");
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String action = intent.getAction();
      if (action != null) {
        switch (action) {
          case ACTION_START_FOREGROUND_SERVICE:
            startForegroundService();
            Toast.makeText(getApplicationContext(), "Foreground service is started.",
                Toast.LENGTH_LONG).show();
            break;
          case ACTION_STOP_FOREGROUND_SERVICE:
            stopForegroundService();
            Toast.makeText(getApplicationContext(), "Foreground service is stopped.",
                Toast.LENGTH_LONG).show();
            break;
          case ACTION_PLAY:
            Toast.makeText(getApplicationContext(), "You click Play button.", Toast.LENGTH_LONG)
                .show();
            break;
          case ACTION_PAUSE:
            Toast.makeText(getApplicationContext(), "You click Pause button.", Toast.LENGTH_LONG)
                .show();
            break;
        }
      }
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }/* Used to build and start foreground service. */

  private void startForegroundService() {
    Log.d(TAG_FOREGROUND_SERVICE, "Start foreground service.");

    // Create notification default intent.
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

    // Create notification builder.
    NotificationCompat.Builder builder;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      String channelName = "My Background Service";
      NotificationChannel nc = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,
          NotificationManager.IMPORTANCE_NONE);
      NotificationManager manager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      assert manager != null;
      manager.createNotificationChannel(nc);

      builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
    } else {
      // If earlier version channel ID is not used
      // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
      builder = new NotificationCompat.Builder(this);
    }
    // Make notification show big text.
    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
    bigTextStyle.setBigContentTitle("Music player implemented by foreground service.");
    bigTextStyle.bigText(
        "Android foreground service is a android service which can run in foreground always, it can be controlled by user via notification.");
    // Set big text style.
    builder.setStyle(bigTextStyle);

    builder.setWhen(System.currentTimeMillis());
    builder.setSmallIcon(R.mipmap.ic_launcher);
    Bitmap largeIconBitmap =
        BitmapFactory.decodeResource(getResources(), android.R.drawable.stat_sys_speakerphone);
    builder.setLargeIcon(largeIconBitmap);
    // Make the notification max priority.
    builder.setPriority(Notification.PRIORITY_MAX);
    // Make head-up notification.
    builder.setFullScreenIntent(pendingIntent, true);

    // Add Play button intent in notification.
    Intent playIntent = new Intent(this, MyForegroundService.class);
    playIntent.setAction(ACTION_PLAY);
    PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
    NotificationCompat.Action playAction =
        new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pendingPlayIntent);
    builder.addAction(playAction);

    // Add Pause button intent in notification.
    Intent pauseIntent = new Intent(this, MyForegroundService.class);
    pauseIntent.setAction(ACTION_PAUSE);
    PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0);
    NotificationCompat.Action prevAction =
        new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause",
            pendingPrevIntent);
    builder.addAction(prevAction);

    // Build the notification.
    Notification notification = builder.build();

    // Start foreground service.
    startForeground(1, notification);

    mAndroidWebServer = new AndroidWebServer();
    try {
      mAndroidWebServer.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void stopForegroundService() {
    Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");

    // Stop foreground service and remove the notification.
    stopForeground(true);

    // Stop the foreground service.
    stopSelf();

    mAndroidWebServer.stop();

    mRecorder.stopCapture();
  }
}
