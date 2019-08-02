package com.temax.wishlist.controller;

import lombok.Data;

@Data
public class NewWishItemRequest {
    private String name;
    private String description;
}
