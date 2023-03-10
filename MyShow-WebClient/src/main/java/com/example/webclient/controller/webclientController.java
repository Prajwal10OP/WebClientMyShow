package com.example.webclient.controller;

import java.time.Duration;

import javax.annotation.PostConstruct;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.webclient.model.BookRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bookMyShow-client")
public class webclientController {

	// HttpClient httpClient = HttpClient.create().secure();
	// ClientHttpConnector httpConnector = new
	// ReactorClientHttpConnector(httpClient);

	WebClient webClient;
	                    /*
						 * = (WebClient) WebClient.builder() .clientConnector(new
						 * ReactorClientHttpConnector(HttpClient.create().doOnConnected(connection ->
						 * connection .addHandler(new ReadTimeoutHandler(10)).addHandler(new
						 * WriteTimeoutHandler(10)))));
						 */

	@PostConstruct
	public void init() {
		webClient = WebClient.builder().baseUrl("http://localhost:8082/BookMyShow/Service")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();

	}

	@PostMapping("/bookNow")
	public Mono<String> BookNow(@RequestBody BookRequest request) {
		return webClient.post().uri("/bookingShow").syncBody(request).retrieve().bodyToMono(String.class).timeout(Duration.ofMillis(5000));

	}

	@GetMapping("/trackBookings")
	public Flux<BookRequest> trackAllBooking() {
		return webClient.get().uri("/getAllBooking").retrieve().bodyToFlux(BookRequest.class);
	}

	@GetMapping("/trackBooking/{bookingId}")
	public Mono<BookRequest> getBookingById(@PathVariable int bookingId) {
		return webClient.get().uri("/getBooking/" + bookingId).retrieve()
				.onStatus(HttpStatus::is4xxClientError,
						clientResponse -> Mono.error(new MyShowClientException("404 unsuported Request")))
				.onStatus(HttpStatus::is5xxServerError,
						clientResponse -> Mono
								.error(new MyShowClientException("505 Internal Error Please Enter Valid ID")))
				.bodyToMono(BookRequest.class);
	}

	@DeleteMapping("/removeBooking/{bookingId}")
	public Mono<String> cancelBooking(@PathVariable int bookingId) {
		return webClient.delete().uri("/cancelBooking/" + bookingId).retrieve()
				.onStatus(HttpStatus::is4xxClientError,
						clientResponse -> Mono.error(new MyShowClientException("404 unsuported Request")))
				.onStatus(HttpStatus::is5xxServerError,
						clientResponse -> Mono
								.error(new MyShowClientException("505 Internal Error Please Enter Valid ID")))
				.bodyToMono(String.class);
	}

	@PutMapping("/changeBooking/{bookingId}")
	public Mono<BookRequest> updateBooking(@PathVariable int bookingId, @RequestBody BookRequest request) {
		return webClient.put().uri("/updateBooking/" + bookingId).syncBody(request).retrieve()
				.onStatus(HttpStatus::is4xxClientError,
						clientResponse -> Mono.error(new MyShowClientException("404 unsuported Request")))
				.onStatus(HttpStatus::is5xxServerError,
						clientResponse -> Mono
								.error(new MyShowClientException("505 Internal Error Please Enter Valid ID")))
				.bodyToMono(BookRequest.class);
	}

}
