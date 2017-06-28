package co.bwsc.pass.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import co.bwsc.pass.R;
import co.bwsc.pass.activity.MainActivity;
import co.bwsc.pass.activity.PopupDialogActivity;
import co.bwsc.pass.networking.PassNetworking;

public class PassService extends Service {

    public static final String TAG = PassService.class.getSimpleName();
    public static final int PASS_SERVICE_NOTIFICATION_ID = 9898;
    private static final int TOAST_DURATION = 5;
    private static final int TOAST_SPLIT_LENGTH = 80;
    private static final int HANDSHAKE_TIMER = 45;
    private static final short PASS_CLIENT_LISTEN_PORT = 1257;
    private static final String PASS_CLIENT_MESSAGE_SIGNATURE = "PASS_MSG" + PassNetworking.DELIMITER;

    private AsyncTask<Void, String, Void> listeningTask = null;
    private PassNetworking networking = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called.");
        Toast.makeText(this, "PassService Started", Toast.LENGTH_LONG).show();

        runAsForeground();

        performHandshakeWithServer();
    }

    private void runAsForeground() {
        Log.d(TAG, getString(R.string.running_as_foreground));
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.service_name))
                .setContentText(getString(R.string.running))
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .setTicker("PASS Service")
                .build();

        startForeground(PASS_SERVICE_NOTIFICATION_ID, notification);

//        showPopUp("What is Lorem Ipsum?\n" +
//                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\n" +
//                "Why do we use it?\n" +
//                "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).");
//        showToast("What is Lorem Ipsum?\n" +
//                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\n" +
//                "Why do we use it?\n" +
//                "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).", 20);
    }

    private void performHandshakeWithServer() {
        Timer timer = new Timer("timer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                networking = new PassNetworking();
                networking.performHandshakeWithServer();
                if (listeningTask != null) {
                    listeningTask.cancel(true);
                }
                startListening();
            }
        }, 0, HANDSHAKE_TIMER * 1000);
    }

    private void startListening() {
        listeningTask = new AsyncTask<Void, String, Void>() {
            private DatagramSocket datagramSocket;

            @Override
            protected void onCancelled() {
                if (datagramSocket != null) {
                    datagramSocket.close();
                }
            }

            @Override
            protected void onProgressUpdate(String... result) {
                if (result != null) {
                    // Structure: SIG | TYPE | MESSAGE | PARAMS
                    String[] payload = result[0].split(PassNetworking.DELIMITER);
                    String type = payload[1];
                    String text = payload[2];
                    String props = payload[3];

                    switch (type.toUpperCase()) {
                        case "TOAST":
                            showToast(text, Double.parseDouble(props));
                            break;
                        case "POPUP":
                            showPopUp(text);
                            break;
                    }
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    datagramSocket = new DatagramSocket(new InetSocketAddress(PASS_CLIENT_LISTEN_PORT));

                    while (true) {
                        byte[] receivedData = new byte[1024];
                        DatagramPacket datagramPacket = new DatagramPacket(receivedData, receivedData.length);
                        if (listeningTask != this) {
                            datagramSocket.close();
                            break;
                        }
                        datagramSocket.receive(datagramPacket);
                        if (listeningTask != this) {
                            datagramSocket.close();
                            break;
                        }

                        String received = new String(datagramPacket.getData(), "UTF-8").trim();

                        if (!received.startsWith(PASS_CLIENT_MESSAGE_SIGNATURE)) {
                            Log.e(TAG, "Invalid message received from " + datagramPacket.getAddress().toString() + "!");
                            continue;
                        }

                        publishProgress(received);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error listening for incoming connections!", e);
                }
                return null;
            }
        };
        listeningTask.execute();
    }

    private void showPopUp(String text) {
        Intent intent = new Intent(this, PopupDialogActivity.class);
        intent.putExtra(PopupDialogActivity.TEXT_EXTRA, text.trim());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

//    private List<String> splitToast(String text) {
//        List<String> split = new ArrayList<>();
//        split.addAll(Arrays.asList(text.split("\n")));
//        for (int i = 0; i < split.size(); i++) {
//            String s = split.get(i);
//            split.remove(i);
//            split.addAll(i, recursiveSplit(s));
//        }
//        return split;
//    }
//
//    private List<String> recursiveSplit(String text) {
//        if (text.trim().length() <= 0) {
//            return new ArrayList<>();
//        }
//
//        if (text.length() > TOAST_SPLIT_LENGTH) {
//            int splitIndex = text.substring(0, TOAST_SPLIT_LENGTH).lastIndexOf(" ");
//            String s1 = text.substring(0, splitIndex).trim();
//            String s2 = text.substring(splitIndex);
//
//            List<String> split = recursiveSplit(s2);
//            split.add(0, s1);
//            return split;
//        }
//
//        List<String> split = new ArrayList<>();
//        split.add(text.trim());
//        return split;
//    }
//
//    private void makeToast(final List<String> text) {
//        if (text == null || text.isEmpty()) {
//            return;
//        }
//
//        final Toast toast = Toast.makeText(this, text.remove(0), Toast.LENGTH_LONG);
//        // Set the toast and duration
//        int toastDurationInMilliSeconds = TOAST_DURATION * 1000;
//
//        // Set the countdown to display the toast
//        CountDownTimer toastCountDown;
//        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
//            public void onTick(long millisUntilFinished) {
//                toast.show();
//            }
//
//            public void onFinish() {
//                toast.cancel();
//                makeToast(text);
//            }
//        };
//
//        // Show the toast and starts the countdown
//        toast.show();
//        toastCountDown.start();
//    }
//
//    private void showToast(String text) {
//        makeToast(splitToast(text.trim()));
//    }

    private void showToast(String text, double seconds) {
        final Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        // Set the toast and duration
        int toastDurationInMilliSeconds = (int) (seconds * 1000);

        // Set the countdown to display the toast
        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
            public void onTick(long millisUntilFinished) {
                toast.show();
            }

            public void onFinish() {
                toast.cancel();
            }
        };

        // Show the toast and starts the countdown
        toast.show();
        toastCountDown.start();
    }

}