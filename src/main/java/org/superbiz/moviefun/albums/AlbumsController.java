package org.superbiz.moviefun.albums;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        final Blob blob = new Blob(format("covers/%d", albumId), uploadedFile.getInputStream(), uploadedFile.getContentType());
        this.blobStore.put(blob);
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        Optional<Blob> blob = this.blobStore.get(format("covers/%d", albumId));

        Blob properBlob = blob.orElseThrow(() -> new IOException("File not found"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(properBlob.getInputStream(), out);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(properBlob.getContentType()));


        byte[] fileBytes = out.toByteArray();
        headers.setContentLength(fileBytes.length);

        return new HttpEntity<>(fileBytes, headers);
    }

}
