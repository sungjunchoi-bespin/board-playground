package com.conduit.tag.domain.port.out;

import com.conduit.tag.domain.model.Tag;
import java.util.List;

public interface TagRepository {

  List<Tag> findAll();
}
