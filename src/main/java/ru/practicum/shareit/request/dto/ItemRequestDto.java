package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.Create;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemRequestDto {
    @NotBlank(groups = {Create.class})
    String description;
}
