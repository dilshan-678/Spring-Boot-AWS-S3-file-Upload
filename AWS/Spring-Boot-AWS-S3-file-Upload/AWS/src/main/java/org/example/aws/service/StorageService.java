package org.example.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 as3Client;

    public String uploadFile(MultipartFile file){

        File fileObject=convertMultiPartFileToFile(file);

        String fileName = System.currentTimeMillis()+"_"+file.getOriginalFilename();

        as3Client.putObject(bucketName,fileName,fileObject);

        fileObject.delete();

        return  "File Uploaded :" +fileName;

    }

    private File convertMultiPartFileToFile(MultipartFile file){

        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));

        try (FileOutputStream fileOutputStream = new FileOutputStream(convertedFile)){

            fileOutputStream.write(file.getBytes());
        }catch (IOException e){
            log.error("Error converting multipartFile to file",e);
        }

        return convertedFile;
    }


    public byte[] downloadFile(String fileName){

        S3Object s3Object=as3Client.getObject(bucketName,fileName);

        S3ObjectInputStream inputStream=s3Object.getObjectContent();

        try {
            return IOUtils.toByteArray(inputStream);
        }catch (IOException e){
            log.error("Error Downloading file",e);
        }

        return null;
    }

    public String deleteFile(String fileName){

        as3Client.deleteObject(bucketName,fileName);

        return fileName + "Removed...";
    }
}
