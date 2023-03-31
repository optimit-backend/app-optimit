package uz.pdp.springsecurity.entity;

import lombok.Data;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;

@Data
@Entity
public class FileData extends AbsEntity {

    private String fileName;

    private byte[] fileData;
}
