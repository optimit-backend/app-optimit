package uz.pdp.springsecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.entity.FileData;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.repository.FileDateRepository;

import java.util.UUID;

@Service
public class FileService {

    @Autowired
    FileDateRepository fileDateRepository;

    public ApiResponse saveFileToDatabase(String fileName, byte[] fileData) {
        FileData fileDataEntity = new FileData();
        fileDataEntity.setFileName(fileName);
        fileDataEntity.setFileData(fileData);
        fileDateRepository.save(fileDataEntity);
        return new ApiResponse("Saved",true);
    }

    public FileData getFileFromDatabase(UUID fileId) {
        return fileDateRepository.findById(fileId).orElse(null);
    }
}
