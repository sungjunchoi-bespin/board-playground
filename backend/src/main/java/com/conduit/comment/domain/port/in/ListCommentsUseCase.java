package com.conduit.comment.domain.port.in;

import com.conduit.comment.domain.model.Comment;

import java.util.List;

public interface ListCommentsUseCase {

    List<Comment> listComments(String slug);
}
