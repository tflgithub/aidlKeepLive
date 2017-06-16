package com.cn.tfl.aidlkeeplive;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class RemoteService extends Service {

    private static final String TAG = "RemoteService";

    private RemoteBinder remoteBinder;

    private RemoteServiceConnection remoteServiceConnection;

    @Override
    public void onCreate() {
        remoteBinder = new RemoteBinder();
        if (remoteServiceConnection == null) {
            remoteServiceConnection = new RemoteServiceConnection();
        }
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return remoteBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindService(new Intent(this, LocalService.class), remoteServiceConnection, Context.BIND_IMPORTANT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("双进程守护服务");
        Notification notification = builder.build();
        // 设置service为前台进程，避免手机休眠时系统自动杀掉该服务
        startForeground(startId, notification);
        return START_STICKY;
    }

    class RemoteBinder extends IProcessService.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return "远程服务";
        }
    }


    class RemoteServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IProcessService processService = IProcessService.Stub.asInterface(iBinder);
            try {
                Log.d(TAG, "连接" + processService.getServiceName() + "成功");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "本地服务被干死了..");
            startService(new Intent(RemoteService.this, LocalService.class));
            bindService(new Intent(RemoteService.this, LocalService.class), remoteServiceConnection, Context.BIND_IMPORTANT);
        }
    }


    @Override
    public void onDestroy() {
        unbindService(remoteServiceConnection);
        super.onDestroy();
    }
}
