
/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
   * Get all the restaurants that are open now within a specific service radius.
   * - For peak hours: 8AM - 10AM, 1PM-2PM, 7PM-9PM
   * - service radius is 3KMs.
   * - All other times, serving radius is 5KMs.
   * - If there are no restaurants, return empty list of restaurants.
   * @param getRestaurantsRequest valid lat/long
   * @param currentTime current time.
   * @return GetRestaurantsResponse object containing a list of open restaurants or an
   *     empty list if none fits the criteria.
   */
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
        LocalTime time8 = LocalTime.of(8,0);
        LocalTime time10 = LocalTime.of(10,0);

        LocalTime time13 = LocalTime.of(13,0);
        LocalTime time14 = LocalTime.of(14,0);

        LocalTime time19 = LocalTime.of(19,0);
        LocalTime time21 = LocalTime.of(21,0);

        List<Restaurant> restaurants;

        if(currentTime.isAfter(time8) && currentTime.isBefore(time10) 
          || currentTime.isAfter(time13) && currentTime.isBefore(time14) 
            || currentTime.isAfter(time19) && currentTime.isBefore(time21)
            || currentTime.equals(time8) || currentTime.equals(time10)
            || currentTime.equals(time13) || currentTime.equals(time14)
            || currentTime.equals(time19) || currentTime.equals(time21)){
              restaurants = new ArrayList<>(restaurantRepositoryService.findAllRestaurantsCloseBy(getRestaurantsRequest.getLatitude(),getRestaurantsRequest.getLongitude(), currentTime, 3.0));
            }
            else{
              restaurants = new ArrayList<>(restaurantRepositoryService.findAllRestaurantsCloseBy(getRestaurantsRequest.getLatitude(),getRestaurantsRequest.getLongitude(), currentTime, 5.0));
            }
            
     return new GetRestaurantsResponse(restaurants);
  }


}

