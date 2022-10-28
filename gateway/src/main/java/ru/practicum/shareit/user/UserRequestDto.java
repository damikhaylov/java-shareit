package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;

import javax.validation.constraints.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserRequestDto {
	private Long id;
	@NotBlank(groups = {Create.class})
	private String name;
	@NotNull(groups = {Create.class})
	@Email(groups = {Create.class, Update.class})
	private String email;
}
