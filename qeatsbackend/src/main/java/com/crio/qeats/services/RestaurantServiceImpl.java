
/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exceptions.QEatsAsyncException;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  /**
   * Get all the restaurants that are open now within a specific service radius. - For peak hours:
   * 8AM - 10AM, 1PM-2PM, 7PM-9PM - service radius is 3KMs. - All other times, serving radius is
   * 5KMs. - If there are no restaurants, return empty list of restaurants.
   * 
   * @param getRestaurantsRequest valid lat/long
   * @param currentTime current time.
   * @return GetRestaurantsResponse object containing a list of open restaurants or an empty list if
   *         none fits the criteria.
   */
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    LocalTime time8 = LocalTime.of(8, 0);
    LocalTime time10 = LocalTime.of(10, 0);

    LocalTime time13 = LocalTime.of(13, 0);
    LocalTime time14 = LocalTime.of(14, 0);

    LocalTime time19 = LocalTime.of(19, 0);
    LocalTime time21 = LocalTime.of(21, 0);

    List<Restaurant> restaurants;
    System.out.println("Inside Service");
    if (currentTime.isAfter(time8) && currentTime.isBefore(time10)
        || currentTime.isAfter(time13) && currentTime.isBefore(time14)
        || currentTime.isAfter(time19) && currentTime.isBefore(time21) || currentTime.equals(time8)
        || currentTime.equals(time10) || currentTime.equals(time13) || currentTime.equals(time14)
        || currentTime.equals(time19) || currentTime.equals(time21)) {
      restaurants = new ArrayList<>(
          restaurantRepositoryService.findAllRestaurantsCloseBy(getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude(), currentTime, peakHoursServingRadiusInKms));
    } else {
      restaurants = new ArrayList<>(
          restaurantRepositoryService.findAllRestaurantsCloseBy(getRestaurantsRequest.getLatitude(),
              getRestaurantsRequest.getLongitude(), currentTime, normalHoursServingRadiusInKms));
    }

    return new GetRestaurantsResponse(restaurants);
  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    if (getRestaurantsRequest.getSearchFor() == null || getRestaurantsRequest.getSearchFor() == "")
      return new GetRestaurantsResponse(Collections.emptyList());
    LocalTime time8 = LocalTime.of(8, 0);
    LocalTime time10 = LocalTime.of(10, 0);

    LocalTime time13 = LocalTime.of(13, 0);
    LocalTime time14 = LocalTime.of(14, 0);

    LocalTime time19 = LocalTime.of(19, 0);
    LocalTime time21 = LocalTime.of(21, 0);

    List<Restaurant> restaurants;
    System.out.println("Inside Service");
    if (currentTime.isAfter(time8) && currentTime.isBefore(time10)
        || currentTime.isAfter(time13) && currentTime.isBefore(time14)
        || currentTime.isAfter(time19) && currentTime.isBefore(time21) || currentTime.equals(time8)
        || currentTime.equals(time10) || currentTime.equals(time13) || currentTime.equals(time14)
        || currentTime.equals(time19) || currentTime.equals(time21)) {
      try {
        CompletableFuture<List<Restaurant>> result1 =
            CompletableFuture.supplyAsync(() -> restaurantRepositoryService.findRestaurantsByName(
                getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
                getRestaurantsRequest.getSearchFor(), currentTime, peakHoursServingRadiusInKms));
        restaurants = new ArrayList<>(result1.get());
        CompletableFuture<List<Restaurant>> result2 = CompletableFuture
            .supplyAsync(() -> restaurantRepositoryService.findRestaurantsByAttributes(
                getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
                getRestaurantsRequest.getSearchFor(), currentTime, peakHoursServingRadiusInKms));
        restaurants.addAll(new ArrayList<>(result2.get()));
      } catch (InterruptedException | ExecutionException e) {
        throw new QEatsAsyncException("Exception in running parallel execution of Services");
      }
    } else {
      try {
        CompletableFuture<List<Restaurant>> result1 =
            CompletableFuture.supplyAsync(() -> restaurantRepositoryService.findRestaurantsByName(
                getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
                getRestaurantsRequest.getSearchFor(), currentTime, normalHoursServingRadiusInKms));
        restaurants = new ArrayList<>(result1.get());
        CompletableFuture<List<Restaurant>> result2 = CompletableFuture
            .supplyAsync(() -> restaurantRepositoryService.findRestaurantsByAttributes(
                getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
                getRestaurantsRequest.getSearchFor(), currentTime, normalHoursServingRadiusInKms));
        restaurants.addAll(new ArrayList<>(result2.get()));
      } catch (InterruptedException | ExecutionException e) {
        throw new QEatsAsyncException("Exception in running parallel execution of Services");
      }
    }


    return new GetRestaurantsResponse(removeDuplicates(restaurants));
  }

  private List<Restaurant> removeDuplicates(List<Restaurant> restaurantList) {
    Set<String> seenIds = new HashSet<>();
    List<Restaurant> uniqueRestaurantList = new ArrayList<>();

    for (Restaurant restaurant : restaurantList) {
      if (seenIds.add(restaurant.getId())) {
        uniqueRestaurantList.add(restaurant);
      }
    }

    return uniqueRestaurantList;
  }

  // TODO: CRIO_TASK_MODULE_MULTITHREADING
  // Implement multi-threaded version of RestaurantSearch.
  // Implement variant of findRestaurantsBySearchQuery which is at least 1.5x time faster than
  // findRestaurantsBySearchQuery.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQueryMt(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

    return null;
  }
}

