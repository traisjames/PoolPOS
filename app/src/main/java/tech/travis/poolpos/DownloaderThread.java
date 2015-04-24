package tech.travis.poolpos;

import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

public class DownloaderThread extends Thread {
    // constants
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    StringBuffer txtfile = new StringBuffer();
    // instance variables
    private MainActivity parentActivity;
    private String downloadUrl;

    /**
     * Instantiates a new DownloaderThread object.
     *
     * @param inParentActivity Reference to MainActivity activity.
     * @param inUrl            String representing the URL of the file to be downloaded.
     */
    public DownloaderThread(MainActivity inParentActivity, String inUrl) {
        Log.i("Starting", getMethodName());
        downloadUrl = "";
        if (inUrl != null) {
            downloadUrl = inUrl;
        }
        parentActivity = inParentActivity;
        Log.i("Finished", getMethodName());
    }

    //Added for DEBUGGING
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getFileName() + " " + Thread.currentThread().getStackTrace()[3].getMethodName() + " at " + Thread.currentThread().getStackTrace()[3].getLineNumber();

    }

    public String getTxtfile() {
        return txtfile.toString();
    }

    /**
     * Connects to the URL of the file, begins the download, and notifies the
     * MainActivity activity of changes in state. Writes the file to
     * the root of the SD card.
     */
    @Override
    public void run() {
        Log.i("Starting", getMethodName());
        URL url;
        URLConnection conn;
        BufferedInputStream inStream;
        Message msg = null;
        // we're going to connect now

        //txtfile.append("2015/04/23 20:15\n" + "KDEH 232015Z AUTO 29007G15KT 250V320 10SM CLR 11/M12 A3002 RMK AO2");
        String errMsg;
        try {

            url = new URL(downloadUrl);
            conn = url.openConnection();
            conn.setUseCaches(false);

            // notify download start

            // start download
            inStream = new BufferedInputStream(conn.getInputStream());

            byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
            int bytesRead = 0;

            while (!isInterrupted() && (bytesRead = inStream.read(data, 0, data.length)) >= 0) {
                txtfile.append(new String(data, 0, bytesRead));
                Log.i("Looping", new String(data, 0, bytesRead));
            }

            inStream.close();

            Log.i("Finished", getMethodName());
            return;
        } catch (MalformedURLException e) {
            errMsg = "Bad URL";

            e.printStackTrace();
        } catch (UnknownHostException e) {
            errMsg = "URL Not Found";

            txtfile.append("2015/04/23 20:15\n" + "KDEH 232015Z AUTO 29007G15KT 250V320 10SM CLR 11/M12 A3002 RMK AO2");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            errMsg = "File not Found";
        } catch (Exception e) {
            errMsg = "General Error Message";
            e.printStackTrace();
        }
        Log.i("Errored", errMsg);
    }


    //    public void runToFile()
    //    {
    //        Log.i("Starting", getMethodName());
    //        URL url;
    //        URLConnection conn;
    //        int fileSize, lastSlash;
    //        String fileName;
    //        BufferedInputStream inStream;
    //        BufferedOutputStream outStream;
    //        File outFile;
    //        FileOutputStream fileStream;
    //        Message msg = null;
    //        // we're going to connect now
    //        /*msg = Message.obtain(parentActivity.activityHandler,MainActivity.MESSAGE_CONNECTING_STARTED,0, 0, downloadUrl);
    //        parentActivity.activityHandler.sendMessage(msg);*/
    //        //txtfile.append("2015/04/23 20:15\n" + "KDEH 232015Z AUTO 29007G15KT 250V320 10SM CLR 11/M12 A3002 RMK AO2");
    //
    //        try
    //        {
    //
    //            url = new URL(downloadUrl);
    //            conn = url.openConnection();
    //            conn.setUseCaches(false);
    //            fileSize = conn.getContentLength();
    //
    //            /*// get the filename
    //            lastSlash = url.toString().lastIndexOf('/');
    //            fileName = "file.bin";
    //            if(lastSlash >=0)
    //            {
    //                fileName = url.toString().substring(lastSlash + 1);
    //            }
    //            if(fileName.equals(""))
    //            {
    //                fileName = "file.bin";
    //            }*/
    //
    //            // notify download start
    //            int fileSizeInKB = fileSize / 1024;
    //            /*msg = Message.obtain(parentActivity.activityHandler,MainActivity.MESSAGE_DOWNLOAD_STARTED,fileSizeInKB, 0, "unfile");
    //            parentActivity.activityHandler.sendMessage(msg);*/
    //            // start download
    //            inStream = new BufferedInputStream(conn.getInputStream());
    //            //            outFile = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
    //            //            fileStream = new FileOutputStream(outFile);
    //            //            outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
    //            byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
    //            int bytesRead = 0, totalRead = 0;
    //
    //            while(!isInterrupted() && (bytesRead = inStream.read(data, 0, data.length)) >= 0)
    //            {
    //                txtfile.append(new String(data,0,bytesRead));
    //                Log.i("Looping", new String(data,0,bytesRead));
    //                //                outStream.write(data, 0, bytesRead);
    //                // update progress bar
    //                totalRead += bytesRead;
    //                int totalReadInKB = totalRead / 1024;
    //                /*msg = Message.obtain(parentActivity.activityHandler,MainActivity.MESSAGE_UPDATE_PROGRESS_BAR,totalReadInKB, 0);
    //                parentActivity.activityHandler.sendMessage(msg);*/
    //            }
    //
    //            //            outStream.close();
    //            //            fileStream.close();
    //            inStream.close();
    //            Log.i("Closed", getMethodName());
    //            if(isInterrupted())
    //            {
    //                // the download was canceled, so let's delete the partially downloaded file
    //                //                outFile.delete();
    //            }
    //            else
    //            {
    //                // notify completion
    //                /*msg = Message.obtain(parentActivity.activityHandler,
    //                        MainActivity.MESSAGE_DOWNLOAD_COMPLETE);
    //                parentActivity.activityHandler.sendMessage(msg);*/
    //            }
    //            Log.i("Finished", getMethodName());
    //            return;
    //        }
    //        catch(MalformedURLException e)
    //        {
    //            String errMsg = "Bad URL";
    //            /*msg = Message.obtain(parentActivity.activityHandler,
    //                    MainActivity.MESSAGE_ENCOUNTERED_ERROR,
    //                    0, 0, errMsg);
    //            parentActivity.activityHandler.sendMessage(msg);*/
    //            e.printStackTrace();
    //        }
    //        catch(UnknownHostException e)
    //        {
    //            String errMsg = "URL Not Found";
    //            /*msg = Message.obtain(parentActivity.activityHandler,
    //                    MainActivity.MESSAGE_ENCOUNTERED_ERROR,
    //                    0, 0, errMsg);
    //            parentActivity.activityHandler.sendMessage(msg);*/
    //            txtfile.append("2015/04/23 20:15\n" + "KDEH 232015Z AUTO 29007G15KT 250V320 10SM CLR 11/M12 A3002 RMK AO2");
    //            e.printStackTrace();
    //        }
    //        catch(FileNotFoundException e)
    //        {
    //            String errMsg = "File not Found";
    //            /*msg = Message.obtain(parentActivity.activityHandler, MainActivity.MESSAGE_ENCOUNTERED_ERROR, 0, 0, errMsg);
    //            parentActivity.activityHandler.sendMessage(msg);*/
    //        }
    //        catch(Exception e)
    //        {
    //            String errMsg = "General Error Message";
    //            /*msg = Message.obtain(parentActivity.activityHandler,
    //                    MainActivity.MESSAGE_ENCOUNTERED_ERROR,
    //                    0, 0, errMsg);
    //
    //            parentActivity.activityHandler.sendMessage(msg);*/
    //            e.printStackTrace();
    //        }
    //        Log.i("Errored", getMethodName());
    //    }
}