package com.kevin.zhangchao.downloder.core;

import com.kevin.zhangchao.downloder.utils.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ZhangChao on 2017/5/13.
 */

public class ConnectThread implements Runnable {

    private final String url;
    private final ConnectListener listener;
    private volatile boolean isRunning;

    public ConnectThread(String url, ConnectListener listener) {
        this.url=url;
        this.listener=listener;
    }

    @Override
    public void run() {
        isRunning=true;
        HttpURLConnection connection=null;
        try {
            connection= (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Range","bytes=0-"+Integer.MAX_VALUE);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode=connection.getResponseCode();
            int totalLength=connection.getContentLength();
            boolean isSupportRange=false;
            if (responseCode==HttpURLConnection.HTTP_OK){
                String ranges=connection.getHeaderField("Accept-Ranges");
                if ("bytes".equals(ranges))
                    isSupportRange=true;
                listener.onConnected(isSupportRange,totalLength);
            }else{
                listener.onConnectError("server error:"+responseCode);
            }
            isRunning=false;
        } catch (IOException e) {
            e.printStackTrace();
            isRunning=false;
            listener.onConnectError(e.getMessage());
        }finally {
            if (connection!=null){
                connection.disconnect();
            }
        }
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public void cancel(){
        Thread.currentThread().interrupt();
    }

    public interface ConnectListener{
        void onConnected(boolean isSupportRange,int totalLength);
        void onConnectError(String message);
    }
}
