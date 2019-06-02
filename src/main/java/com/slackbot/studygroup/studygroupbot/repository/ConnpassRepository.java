package com.slackbot.studygroup.studygroupbot.repository;

import com.slackbot.studygroup.studygroupbot.domain.ConnpassResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ConnpassRepository {

    public ConnpassResponse getConnpassResponse() {
        RestTemplate restTemplate = new RestTemplate();
        ConnpassResponse connpassResponse = restTemplate.getForObject(getUrl(), ConnpassResponse.class);
        return connpassResponse;
    }

    private String getUrl(){

        return UriComponentsBuilder.fromHttpUrl("https://connpass.com/api/v1/event")
                .queryParam("keyword", "tokyo")
                .queryParam("keyword_or", "東京")
                .queryParam("order", 1)
                .queryParam("count", 1000)
                .build().toUriString();
    }
}
