package ru.practicum.shareit.request;

import lombok.*;
import ru.practicum.shareit.Create;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestRequestDto {
    @NotBlank(groups = {Create.class})
    String description;
}
