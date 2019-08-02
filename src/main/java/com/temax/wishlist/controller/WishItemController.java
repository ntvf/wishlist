package com.temax.wishlist.controller;

import com.temax.wishlist.model.WishItem;
import com.temax.wishlist.model.WishItemStatus;
import com.temax.wishlist.service.WishItemService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/wishItems")
public class WishItemController {
    @Autowired
    private WishItemService wishItemService;

    @GetMapping
    public List<WishItem> getAllItems() {
        return wishItemService.getAllItems();
    }

    @PostMapping("/assignItem")
    public List<WishItem> assignItem(@RequestParam ArrayList<String> itemsId, String email) {
        wishItemService.reserveItems(itemsId, email);
        return getAllItems();
    }

    @PostMapping
    public void createItem(@RequestBody NewWishItemRequest newWishItemRequest) {
        Validate.isTrue(StringUtils.isNotBlank(newWishItemRequest.getName()), "Name should not be empty");
        Validate.isTrue(StringUtils.isNotBlank(newWishItemRequest.getDescription()), "Description should exist");
        wishItemService.saveItem(WishItem.builder()
                .name(newWishItemRequest.getName())
                .description(newWishItemRequest.getDescription())
                .wishItemStatus(WishItemStatus.OPEN)
                .build());
    }

    @GetMapping("/confirmReservation")
    public void confirmReservation(@RequestParam ArrayList<String> itemIds, @RequestParam String email, HttpServletResponse httpServletResponse) {
        Validate.isTrue(CollectionUtils.isNotEmpty(itemIds));
        Validate.isTrue(!StringUtils.isBlank(email));
        itemIds.forEach(itemId -> wishItemService.confirmReservation(itemId, email));
        httpServletResponse.setHeader("Location", "/?reservationSuccess=true");
        httpServletResponse.setStatus(302);
    }
}
