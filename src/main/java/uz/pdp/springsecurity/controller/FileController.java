package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.springsecurity.entity.FileData;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.service.FileService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/files")
    public HttpEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            byte[] fileData = file.getBytes();
            String fileName = file.getOriginalFilename();
            ApiResponse apiResponse = fileService.saveFileToDatabase(fileName, fileData);
            return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE).build();
        }
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable UUID fileId) {
        ApiResponse fileFromDatabase = fileService.getFileFromDatabase(fileId);
        if (fileFromDatabase != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "a" + "\"")
                    .body((byte[]) fileFromDatabase.getObject());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

