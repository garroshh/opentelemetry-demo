package org.daocloud.springcloud.adservice.controller;

import org.daocloud.springcloud.adservice.dto.Advertise;
import org.daocloud.springcloud.adservice.model.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Random;

@RequestMapping("/ad")
@RestController
public class AdController {
    private RestTemplate restTemplate;

    public AdController(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${data_service_name:dataservice}")
    private String dataServiceName;

    @GetMapping("/ad-key/{adKey}")
    public ResponseEntity<Advertise[]> findByAdKey(@PathVariable String key) {
        return this.restTemplate.getForEntity("http://" + dataServiceName + "/ad/ad-key/{key}", Advertise[].class, key);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Advertise> findById(@PathVariable Long id) {
        return this.restTemplate.getForEntity("http://" + dataServiceName + "/ad/{id}", Advertise.class, id);
    }

    @GetMapping("/all")
    public ResponseEntity<Advertise[]> getAllAds() {
        return this.restTemplate.getForEntity("http://" + dataServiceName + "/ad/all", Advertise[].class);
    }

    @GetMapping("/call")
    public Mono<Info> call() {

        Info info = new Info();
        String dataService = System.getenv("CALL_TO_DATA");
        Random random = new Random();
        if (!Objects.equals(dataService, "")) {
            return WebClient.create("http://" + dataService)
                    .get()
                    .uri("/ad/call")
                    .retrieve()
                    .bodyToMono(Info.class)
                    .flatMap(info2 -> {
                        info.setID(String.format("%d%d", System.currentTimeMillis(), random.nextInt(10)));
                        info.setHostName(System.getenv("HOSTNAME"));
                        info.setVersion(System.getenv("VERSION"));
                        info.setTarget(info2);
                        return Mono.just(info);
                    });

        }

        return Mono.just(info);
    }
}
