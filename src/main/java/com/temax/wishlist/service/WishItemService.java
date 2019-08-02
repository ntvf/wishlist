package com.temax.wishlist.service;

import com.temax.wishlist.model.WishItem;

import java.util.List;

public interface WishItemService {
    List<WishItem> getAllItems();

    WishItem saveItem(WishItem item);

    boolean reserveItems(List<String> itemIds, String email);

    void confirmReservation(String itemId, String email);
}
