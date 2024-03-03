package ru.practicum.shareit.item.dto.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentDtoMapperTest {

    private CommentDtoMapper commentDtoMapper;

    @BeforeEach
    void setUp() {
        commentDtoMapper = new CommentDtoMapper();
    }

    @Test
    void toCommentDto() {
        User author = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", false, owner, null);
        Comment comment = new Comment(1L, "text", item, author,
                LocalDateTime.of(2024, 2, 20, 10, 10, 10));
        CommentDto expectedCommentDto = new CommentDto(1L, "text", "Vladimir",
                LocalDateTime.of(2024, 2, 20, 10, 10, 10));

        CommentDto resultCommentDto = commentDtoMapper.toCommentDto(comment);

        assertEquals(expectedCommentDto, resultCommentDto);
    }

    @Test
    void toCommentDtoList_whenCommentsListIfEmpty() {
        List<Comment> commentsList = Collections.emptyList();
        List<CommentDto> resultCommentDtoList = commentDtoMapper.toCommentDtoList(commentsList);

        assertTrue(resultCommentDtoList.isEmpty());
    }

    @Test
    void toCommentDtoListTest() {
        User author = new User(1L, "Vladimir", "vladimir@yandex.ru");
        User owner = new User(2L, "Ilya", "ilya@yandex.ru");
        Item item = new Item(1L, "имя", "описание", false, owner, null);
        List<Comment> commentsList = List.of(new Comment(1L, "text", item, author,
                LocalDateTime.of(2024, 2, 20, 10, 10, 10)));

        List<CommentDto> expectedCommentDtoList = List.of(new CommentDto(1L, "text", "Vladimir",
                LocalDateTime.of(2024, 2, 20, 10, 10, 10)));

        List<CommentDto> resultCommentDtoList = commentDtoMapper.toCommentDtoList(commentsList);

        assertEquals(expectedCommentDtoList, resultCommentDtoList);
    }
}