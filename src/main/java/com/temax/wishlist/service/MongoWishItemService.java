package com.temax.wishlist.service;

import com.temax.wishlist.model.WishItem;
import com.temax.wishlist.model.WishItemStatus;
import de.neuland.jade4j.Jade4J;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MongoWishItemService implements WishItemService {
    private MongoCollection wishItemsCollestion;
    private JavaMailSender mailSender;
    private String frontApiUrl;

    @Value("classpath:mail/template/reserveConfirmation.jade")
    private Resource confirmationTemplate;

    public MongoWishItemService(Jongo jongo, JavaMailSender mailSender, String frontEndUrl) {
        this.wishItemsCollestion = jongo.getCollection("wishItem");
        this.mailSender = mailSender;
        this.frontApiUrl = frontEndUrl;
    }

    @Override
    public List<WishItem> getAllItems() {
        return StreamSupport
                .stream(wishItemsCollestion.find().as(WishItem.class).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public WishItem saveItem(WishItem item) {
        wishItemsCollestion.save(item);
        return item;
    }

    @Override
    @SneakyThrows
    public boolean reserveItems(List<String> itemIds, String email) {
        val wishItems = itemIds.stream().map(this::getWishItem).collect(Collectors.toList());
        wishItems.forEach(it -> {
            it.setAssignedEmail(email);
            it.setWishItemStatus(WishItemStatus.CONFIRMATION_IN_PROGRESS);
            wishItemsCollestion.save(it);
        });
        val jadeContext = new HashMap<String, Object>();
        jadeContext.put("items", wishItems);
        jadeContext.put("activationLink", createActivationLink(wishItems, email));

        val body = Jade4J.render(confirmationTemplate.getURL(), jadeContext);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
            message.setText(body, true);

            message.setFrom("noreply@" + frontApiUrl.split("//")[1].split(":")[0]);

            message.setTo(email);
            message.setSubject(frontApiUrl + " Підтвердження заброньованих подарунків");
        };
        mailSender.send(messagePreparator);
        return true;
    }

    private Object createActivationLink(List<WishItem> items, String email) {
        val params = Stream.concat(items.stream().map(it -> new BasicNameValuePair("itemIds", it.getId())),
                Stream.of(new BasicNameValuePair("email", email))).collect(Collectors.toList());
        return frontApiUrl + "/wishItems/confirmReservation?" + URLEncodedUtils.format(params, StandardCharsets.UTF_8);
    }

    private WishItem getWishItem(String itemId) {
        return wishItemsCollestion.findOne(new ObjectId(itemId)).as(WishItem.class);
    }

    @Override
    public void confirmReservation(String itemId, String email) {
        val item = Optional.ofNullable(getWishItem(itemId))
                .orElseThrow(() -> new IllegalArgumentException("Item doesn't exists"));
        if (!email.equalsIgnoreCase(item.getAssignedEmail())) {
            throw new IllegalStateException("Wrong email");
        }
        item.setWishItemStatus(WishItemStatus.CLOSED);
        wishItemsCollestion.save(item);

    }
}
