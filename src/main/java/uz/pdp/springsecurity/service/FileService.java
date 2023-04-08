package uz.pdp.springsecurity.service;

import uz.pdp.springsecurity.entity.FileData;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.repository.FileDateRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Service
public class FileService {

    @Autowired
    FileDateRepository fileDateRepository;

    public ApiResponse saveFileToDatabase(String fileName, byte[] fileData) {

        int maxSizeInBytes = 1024 * 1024; // 1MB
        if (fileData.length > maxSizeInBytes){
            return new ApiResponse("File too large !",false);
        }

        FileData fileDataEntity = new FileData();
        fileDataEntity.setFileName(fileName);
        fileDataEntity.setFileData(fileData);
        fileDateRepository.save(fileDataEntity);
        return new ApiResponse("Found",true,fileDataEntity.getId());
    }

    public ApiResponse getFileFromDatabase(UUID fileId) {
        FileData fileData = fileDateRepository.findById(fileId).orElse(null);
        return new ApiResponse("Found",true,fileData);
    }
}
