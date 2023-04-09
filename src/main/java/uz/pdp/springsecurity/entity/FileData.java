package uz.pdp.springsecurity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.springsecurity.entity.template.AbsEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FileData extends AbsEntity {

    private String fileName;

    private byte[] fileData;

    private long size;

    @ManyToOne
    Project project;
}
