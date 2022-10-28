package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;

    User owner;
    Item itemA;
    Item itemB;
    Item itemC;
    PageRequest pageRequest;

    @BeforeEach
    void beforeEach() {
        owner = userRepository.save(new User(null, "User Name", "email@email.com"));
        itemA = itemRepository.save(
                new Item(null, "ItemA", "ItemA description", true, owner,null));
        itemB = itemRepository.save(
                new Item(null, "ItemB", "ItemB description", false, owner,null));
        itemC = itemRepository.save(
                new Item(null, "ItemC", "ItemC description", true, owner,null));
        pageRequest = new MyPageRequest(0, 10, Sort.unsorted());
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void searchAvailableItemsTest() {
        final Page<Item> items = itemRepository.search("Item", pageRequest);
        assertEquals(2, items.getTotalElements());
    }

    @Test
    void searchItemsIgnoringCaseTest() {
        final Page<Item> items = itemRepository.search("iTeM", pageRequest);
        assertEquals(2, items.getTotalElements());
    }

    @Test
    void searchItemsByFullNameIgnoringCaseTest() {
        final Page<Item> items = itemRepository.search("itemc", pageRequest);
        assertEquals(1, items.getTotalElements());
    }

    @Test
    void searchItemsByFullDescriptionIgnoringCaseTest() {
        final Page<Item> items = itemRepository.search("ITEMA DESCRIPTION", pageRequest);
        assertEquals(1, items.getTotalElements());
    }

    @Test
    void searchItemsByNonMatchingTextTest() {
        final Page<Item> items = itemRepository.search("Non matching text", pageRequest);
        assertEquals(0, items.getTotalElements());
    }
}
