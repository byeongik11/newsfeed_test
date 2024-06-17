package org.example.newsfeed.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostTest {
    private User user;
    private Post post;

    @BeforeEach
    public void setUp() {
        user = User.builder()
            .userId("testUserId")
            .password("$2a$10$abcdefg1234567")
            .name("Test User")
            .email("test@example.com")
            .build();

        post = Post.builder()
            .user(user)
            .content("Initial content")
            .build();
    }

    @Test
    @DisplayName("빌더 테스트")
    public void testPostBuilder() {
        assertNotNull(post);
        assertEquals("Initial content", post.getContent());
        assertEquals(user, post.getUser());
        assertFalse(post.isDeleted());
        assertNotNull(post.getComments());
        assertTrue(post.getComments().isEmpty());
    }

    @Test
    @DisplayName("내용 테스트")
    public void testSetContent() {
        post.setContent("Updated content");
        assertEquals("Updated content", post.getContent());
    }

    @Test
    @DisplayName("deleted 상태를 true로 변경하는지 확인")
    public void testSetDeleted() {
        post.setDeleted();
        assertTrue(post.isDeleted());
    }

    @Test
    @DisplayName("댓글 목록에 댓글을 추가할 수 있는지 테스트")
    public void testAddComment() {
        Comment comment = new Comment();
        post.getComments().add(comment);

        assertFalse(post.getComments().isEmpty());
        assertEquals(1, post.getComments().size());
        assertTrue(post.getComments().contains(comment));
    }

    @Test
    @DisplayName("댓글 목록에서 댓글을 제거할 수 있는지 테스트")
    public void testRemoveComment() {
        Comment comment = new Comment();
        post.getComments().add(comment);
        post.getComments().remove(comment);

        assertTrue(post.getComments().isEmpty());
    }
}