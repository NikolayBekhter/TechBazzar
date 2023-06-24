package ru.bazzar.picture.converters;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.bazzar.picture.api.PictureDto;
import ru.bazzar.picture.entities.Picture;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PictureConverter {
    private final ModelMapper modelMapper;

    public PictureDto entityToDto(Picture picture) {
        return modelMapper.map(picture, PictureDto.class);
    }

    public Picture dtoToEntity(PictureDto pictureDto) {
        return modelMapper.map(pictureDto, Picture.class);
    }
}
