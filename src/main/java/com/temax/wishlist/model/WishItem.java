package com.temax.wishlist.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishItem {
    @MongoId
    @MongoObjectId
    private String id;
    private String name;
    private String description;
    private WishItemStatus wishItemStatus;
    private String assignedEmail;
}
