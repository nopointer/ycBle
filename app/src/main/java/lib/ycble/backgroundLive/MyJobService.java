package lib.ycble.backgroundLive;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {

    private final static String TAG = "MyJobService";


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JobParameters param = (JobParameters) msg.obj;
            jobFinished(param, true);
            Log.d(TAG, "jobFinished");
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob");
        Message message = Message.obtain();
        message.obj = params;
        mHandler.sendMessage(message);
        //返回false表示执行完毕，返回true表示需要开发者自己调用jobFinished方法通知系统已执行完成
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        //停止，不是结束。jobFinished不会直接触发onStopJob
        //必须在“onStartJob之后，jobFinished之前”取消任务，才会在jobFinished之后触发onStopJob
        Log.d(TAG, "onStopJob");
        mHandler.removeMessages(0);
        return true;
    }

}