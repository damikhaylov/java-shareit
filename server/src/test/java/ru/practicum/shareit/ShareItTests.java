package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureMockMvc
class ShareItTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	private final UserRepository userRepository;
	private final ItemRequestRepository itemRequestRepository;
	private final ItemRepository itemRepository;
	private final BookingRepository bookingRepository;
	private final CommentRepository commentRepository;

	private User ownerUser;
	private User requesterUser;
	private User bookerUser;
	private Item availableItem;
	private Item unavailableItem;
	private Booking booking;
	private final LocalDateTime newBookingStart =
			LocalDateTime.of(2050, 1, 1, 12, 0, 0);
	private final LocalDateTime newBookingEnd =
			LocalDateTime.of(2050, 2, 1, 12, 0, 0);

	@BeforeEach
	void beforeEach() {
		ownerUser = new User(null, "Owner User", "owner@mail.com");
		requesterUser = new User(null, "Requester User", "requester@mail.com");
		bookerUser = new User(null, "Booker User", "booker@mail.com");
		ownerUser = userRepository.save(ownerUser);
		requesterUser = userRepository.save(requesterUser);
		bookerUser = userRepository.save(bookerUser);

		ItemRequest request = new ItemRequest(null, "Request for item", requesterUser,
				LocalDateTime.of(2022, 1, 1, 12, 0, 0));
		request = itemRequestRepository.save(request);

		availableItem = new Item(null, "Item1", "Item1 description", true, ownerUser,
				request);
		unavailableItem = new Item(null, "Item2", "Item2 description", false, ownerUser,
				null);
		availableItem = itemRepository.save(availableItem);
		unavailableItem = itemRepository.save(unavailableItem);

		Booking bookingInPast = new Booking(null,
				LocalDateTime.of(2020, 1, 1, 12, 0, 0),
				LocalDateTime.of(2020, 1, 2, 12, 0, 0),
				availableItem, bookerUser, BookingStatus.APPROVED);
		bookingRepository.save(bookingInPast);

		booking = new Booking(null, newBookingStart, newBookingEnd, availableItem, bookerUser,
				BookingStatus.WAITING);
		booking = bookingRepository.save(booking);
	}

	@AfterEach
	void afterEach() {
		commentRepository.deleteAll();
		bookingRepository.deleteAll();
		itemRequestRepository.deleteAll();
		userRepository.deleteAll();
		itemRepository.deleteAll();
	}

	@Test
	void getAllUsersTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)));
	}

	@Test
	void getNonExistentUserTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/users/100"))
				.andExpect(status().isNotFound());
	}

	@Test
	void searchItemTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/items/search?text=Item"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(availableItem.getId().intValue())));
	}

	@Test
	void getAllItemsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/items")
						.header("X-Sharer-User-Id", ownerUser.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void createBookingForUnavailableItemTest() throws Exception {
		BookingDto bookingDto = new BookingDto(null, newBookingStart, newBookingEnd, unavailableItem.getId(),
				null, null);
		mvc.perform(MockMvcRequestBuilders.post("/bookings")
						.header("X-Sharer-User-Id", bookerUser.getId())
						.content(mapper.writeValueAsString(bookingDto))
						.characterEncoding(StandardCharsets.UTF_8)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createBookingForItemWithNullIdTest() throws Exception {
		BookingDto bookingDto = new BookingDto(null, newBookingStart, newBookingEnd, null,
				null, null);
		mvc.perform(MockMvcRequestBuilders.post("/bookings")
						.header("X-Sharer-User-Id", bookerUser.getId())
						.content(mapper.writeValueAsString(bookingDto))
						.characterEncoding(StandardCharsets.UTF_8)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void createBookingForOwnerAsBookerTest() throws Exception {
		BookingDto bookingDto = new BookingDto(null, newBookingStart, newBookingEnd, availableItem.getId(),
				null, null);
		mvc.perform(MockMvcRequestBuilders.post("/bookings")
						.header("X-Sharer-User-Id", ownerUser.getId())
						.content(mapper.writeValueAsString(bookingDto))
						.characterEncoding(StandardCharsets.UTF_8)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	void approveBookingTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.patch("/bookings/" + booking.getId() + "?approved=true")
						.header("X-Sharer-User-Id", ownerUser.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));

		mvc.perform(MockMvcRequestBuilders.patch("/bookings/" + booking.getId() + "?approved=true")
						.header("X-Sharer-User-Id", ownerUser.getId()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createCommentTest() throws Exception {
		String comment = "comment";
		mvc.perform(MockMvcRequestBuilders.post("/items/" + availableItem.getId() + "/comment")
						.header("X-Sharer-User-Id", bookerUser.getId())
						.content(mapper.writeValueAsString(new CommentDto(comment)))
						.characterEncoding(StandardCharsets.UTF_8)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.text", is(comment)));
	}

	@Test
	void createCommentWithoutBookingTest() throws Exception {
		String comment = "comment";
		mvc.perform(MockMvcRequestBuilders.post("/items/" + availableItem.getId() + "/comment")
						.header("X-Sharer-User-Id", requesterUser.getId())
						.content(mapper.writeValueAsString(new CommentDto(comment)))
						.characterEncoding(StandardCharsets.UTF_8)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}
}

