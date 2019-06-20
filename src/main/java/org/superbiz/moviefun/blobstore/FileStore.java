package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.Files.readAllBytes;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {

        File targetFile = new File(blob.getName());

        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(blob.getInputStream(), out);
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(out.toByteArray());
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        Path coverFilePath = null;
        try {
            coverFilePath = getExistingCoverPath(name);
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
        byte[] imageBytes = readAllBytes(coverFilePath);
        String contentType = new Tika().detect(coverFilePath);
        return Optional.of(new Blob(name, new ByteArrayInputStream(imageBytes), contentType));
    }

    @Override
    public void deleteAll() {
        return;
    }

    private Path getExistingCoverPath(String fileName) throws URISyntaxException {
        File coverFile = new File(fileName);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }


}
