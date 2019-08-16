package com.temax.wishlist;

import com.mongodb.MongoClient;
import com.temax.wishlist.service.MongoWishItemService;
import com.temax.wishlist.service.WishItemService;
import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootApplication
@PropertySource(value = {
        "classpath:/${env}.properties"
}, ignoreResourceNotFound = true)
public class WishlistApp {
    public static void main(String[] args) {
        SpringApplication.run(WishlistApp.class);
    }

    @Bean
    public Jongo jongo() {
        return new Jongo(new MongoClient().getDB("wishlist"));
    }

    @Bean
    public WishItemService wishItemService(Jongo jongo,
                                           JavaMailSender javaMailSender,
                                           @Value("${frontend.url}") String frontEndUrl) {
        return new MongoWishItemService(jongo, javaMailSender, frontEndUrl);
    }

}
