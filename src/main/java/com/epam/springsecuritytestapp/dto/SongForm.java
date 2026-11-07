package com.epam.springsecuritytestapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SongForm {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String title;

    @NotBlank(message = "El artista es obligatorio")
    @Size(max = 200, message = "El artista no puede superar 200 caracteres")
    private String artist;
}
