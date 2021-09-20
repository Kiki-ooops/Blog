package com.kiki.blog.app.controller;

import com.kiki.blog.app.service.ImageService;
import com.kiki.blog.openapi.api.ImageApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;

@RestController
public class ImageController implements ImageApi {

    private final ImageService imageService;
    private final ServletContext servletContext;

    @Autowired
    public ImageController(ImageService imageService, ServletContext servletContext) {
        this.imageService = imageService;
        this.servletContext = servletContext;
    }

    @Override
    public ResponseEntity<String> uploadImage(MultipartFile image) throws Exception {
        String file = imageService.storeFile(image);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource> getImage(String imageId) throws Exception {
        Resource resource = imageService.loadFile(imageId);
        String contentType = servletContext.getMimeType(resource.getFile().getAbsolutePath());

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
