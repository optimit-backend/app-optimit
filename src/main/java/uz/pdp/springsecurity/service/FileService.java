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

    public ApiResponse saveFileToDatabase(String fileName, byte[] fileData,long size) {

        long maxSizeInBytes = 10000L * 1024 * 1024; // 5MB
        if (fileData.length > maxSizeInBytes){
            return new ApiResponse("File too large !",false);
        }

        FileData fileDataEntity = new FileData();
        fileDataEntity.setFileName(fileName);
        fileDataEntity.setFileData(fileData);
        fileDataEntity.setSize(size);
        fileDateRepository.save(fileDataEntity);
        return new ApiResponse("Found",true,fileDataEntity.getId());
    }

    public FileData getFileFromDatabase(UUID fileId) {
        return fileDateRepository.findById(fileId).orElse(null);
    }

    public ApiResponse deleteById(UUID fileId) {
        boolean exists = fileDateRepository.existsById(fileId);
        if (!exists){
            return new ApiResponse("Not found",false);
        }
        fileDateRepository.deleteById(fileId);
        return new ApiResponse("Deleted",true);
    }
}
