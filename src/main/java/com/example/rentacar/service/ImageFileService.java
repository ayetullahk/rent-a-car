package com.example.rentacar.service;

import com.example.rentacar.domain.ImageData;
import com.example.rentacar.domain.ImageFile;
import com.example.rentacar.dto.ImageFileDTO;
import com.example.rentacar.exception.ResourceNotFoundException;
import com.example.rentacar.exception.message.ErrorMessage;
import com.example.rentacar.repository.ImageFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ImageFileService {
    @Autowired
    private ImageFileRepository imageFileRepository;

    public String saveImage(MultipartFile file) {

        ImageFile imageFile = null;

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            ImageData imData = new ImageData(file.getBytes());
            imageFile = new ImageFile(fileName, file.getContentType(), imData);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        imageFileRepository.save(imageFile);
        return imageFile.getId();
    }

    public ImageFile getImageById(String id) {

        ImageFile imageFile = imageFileRepository.findById(id).orElseThrow(() -> new
                ResourceNotFoundException(String.format(ErrorMessage.IMAGE_NOT_FOUND_MESSAGE, id)));

        return imageFile;

    }

    public List<ImageFileDTO> getAllImages() {

        List<ImageFile> imageFiles = imageFileRepository.findAll();

        List<ImageFileDTO> imageFileDTOs = imageFiles.stream().map(imFile -> {
            String imageUri = ServletUriComponentsBuilder.fromCurrentContextPath().
                    path("/files/download/").path(imFile.getId()).toUriString();

            return new ImageFileDTO(imFile.getName(), imageUri, imFile.getType(), imFile.getLength());
        }).collect(Collectors.toList());
        return imageFileDTOs;

    }


    public void removeById(String id) {
        ImageFile imageFile = getImageById(id);
        imageFileRepository.delete(imageFile);

    }

    public ImageFile findImageById(String id) {
        return imageFileRepository.findImageById(id).orElseThrow(() -> new
                ResourceNotFoundException(String.format(ErrorMessage.IMAGE_NOT_FOUND_MESSAGE, id)));

    }

}
