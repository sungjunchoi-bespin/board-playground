package com.conduit.comment.domain.port.in;

import com.conduit.comment.domain.model.Comment;

public interface AddCommentUseCase {

  Comment addComment(String slug, String body, Long authorId);
}
