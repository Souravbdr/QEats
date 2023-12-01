/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.controller;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exceptions.QEatsAsyncException;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.services.RestaurantService;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestaurantController.RESTAURANT_API_ENDPOINT)
@Log4j2
@Validated


public class RestaurantController {

  public static final String RESTAURANT_API_ENDPOINT = "/qeats/v1";
  public static final String RESTAURANTS_API = "/restaurants";
  public static final String MENU_API = "/menu";
  public static final String CART_API = "/cart";
  public static final String CART_ITEM_API = "/cart/item";
  public static final String CART_CLEAR_API = "/cart/clear";
  public static final String POST_ORDER_API = "/order";
  public static final String GET_ORDERS_API = "/orders";

  @Autowired
  private RestaurantService restaurantService;

  @GetMapping(RESTAURANTS_API)
  public ResponseEntity<GetRestaurantsResponse> getRestaurants(
      @Valid GetRestaurantsRequest getRestaurantsRequest) {
    // System.out.println("test");
    System.out.println(getRestaurantsRequest);
    log.info("getRestaurants called with {}", getRestaurantsRequest);
    GetRestaurantsResponse getRestaurantsResponse;

    // CHECKSTYLE:OFF
    getRestaurantsResponse = restaurantService.findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.now());
    if(getRestaurantsRequest.getSearchFor()!=null || getRestaurantsRequest.getSearchFor()!=""){
      GetRestaurantsResponse resp = restaurantService.findRestaurantsBySearchQuery(getRestaurantsRequest, LocalTime.now());
      
      getRestaurantsResponse.getRestaurants().addAll(resp!=null ? resp.getRestaurants():Collections.emptyList());
    }
    List<Restaurant> restaurants = getRestaurantsResponse.getRestaurants();
    for (Restaurant restaurant : restaurants) {
      String sanitizedName = restaurant.getName().replaceAll("[Â©éí]", "e");
      restaurant.setName(sanitizedName);
     }
    log.info("getRestaurants returned {}", getRestaurantsResponse);
    System.out.println(getRestaurantsResponse);
    // CHECKSTYLE:ON

    return ResponseEntity.ok().body(getRestaurantsResponse);
  }

    
}

