package org.superbiz.moviefun.blobstore;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {

    private static final Logger log = LoggerFactory.getLogger(S3Store.class);

    private final AmazonS3Client s3Client;
    private final String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.getContentType());

        try {
            s3Client.putObject(photoStorageBucket,blob.getName(),
                    blob.getInputStream(),objectMetadata);
        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw e;
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        S3Object s3Object = null;
        try {

             s3Object = s3Client.getObject(photoStorageBucket, name);

        }catch (Exception e){
            log.error("Failed to get S3 object", e);
            return Optional.empty();
        }
        Blob blob = new Blob(s3Object.getKey(), s3Object.getObjectContent(), s3Object.getObjectMetadata().getContentType());
        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {
        return;
    }
}
