package com.mumtaz.aws.buikcopys3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.SyncProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class Uploader {

    @Autowired
    Environment environment;

    public AmazonS3 getS3Api() {
        String accesskey = environment.getProperty("accesskey");
        String secret = environment.getProperty("secret");

        BasicAWSCredentials basicAWSCredentials
                = new BasicAWSCredentials(accesskey, secret);

        ClientConfiguration clientConfiguration = new ClientConfiguration()
                .withConnectionTimeout(300000)
                .withMaxErrorRetry(300000)
                .withSocketTimeout(300000)
                .withClientExecutionTimeout(300000);

        return AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .withClientConfiguration(clientConfiguration)
                .build();
    }

    public void transferFilesToAWS (File file, String filename ) {
        String keyName = "someprefix" + filename;
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(getS3Api())
                .build();

        Upload upload = tm.upload("mk-bulk", keyName, file);
        try {
            upload.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tm.shutdownNow();
    }

    public void transferBulkFilesToAWS (File directory) {
        List<File> files = Arrays.asList(directory.listFiles());
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(getS3Api())
                .build();
        String prefix = "";
        try {
            MultipleFileUpload upload = tm.uploadFileList("xmk-bulk", prefix, directory, files);
            upload.addProgressListener(new FailureListener(upload));
            upload.waitForCompletion();

//            do {
//                TransferProgress tp = upload.getProgress();
//                System.out.println("progress:" + tp.getPercentTransferred());
//                try {
//                    TimeUnit.SECONDS.sleep(2);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } while (!upload.isDone());

//            System.out.println("shutting down");
//            tm.shutdownNow();
//            System.out.println("shutting down complete");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class FailureListener extends SyncProgressListener {
        private  MultipleFileUpload upload ;
        public FailureListener(MultipleFileUpload upload) {
            this.upload = upload;
        }

        public void progressChanged(ProgressEvent progressEvent) {
            System.out.println("progress event : " + progressEvent.getEventType());
            if (progressEvent.getEventType() == ProgressEventType.CLIENT_REQUEST_FAILED_EVENT) {
                try {
                    System.out.println("client exception....");
                    //AmazonClientException e = upload.waitForException();
                    //e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void uploadFile() {
        File file = new File("first.txt");
        try {
            BufferedWriter bufferedWriter = null;

            bufferedWriter = new BufferedWriter( new FileWriter(file));

            bufferedWriter.write("some text");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        transferFilesToAWS(file, "myFileName.txt");
    }

    public void bulkUploadFile() {
        File file = new File("first.txt");
        try {
            BufferedWriter bufferedWriter = null;

            bufferedWriter = new BufferedWriter( new FileWriter(file));

            bufferedWriter.write("some text");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        transferBulkFilesToAWS(new File("."));
    }

}
