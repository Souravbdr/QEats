/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.ItemEntity;
import com.crio.qeats.models.MenuEntity;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.ItemRepository;
import com.crio.qeats.repositories.MenuRepository;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {



  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private RedisConfiguration redisConfiguration;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {
    System.out.println("Inside repo Service");
    GeoLocation geoLocation = new GeoLocation(latitude, longitude);
    GeoHash geoHash =
        GeoHash.withCharacterPrecision(geoLocation.getLatitude(), geoLocation.getLongitude(), 7);
    List<Restaurant> restaurants = new ArrayList<>();
    if (redisConfiguration.isCacheAvailable()
        && redisConfiguration.getJedisPool().getResource().exists(geoHash.toBase32())) {
      System.out.println("From Cache");
      restaurants = retrieveRestaurantListFromRedis(redisConfiguration.getJedisPool().getResource(),
          geoHash.toBase32());
    } else {
      System.out.println("From Mongo");
      List<RestaurantEntity> restaurantList = restaurantRepository.findAll();
      for (RestaurantEntity restaurantEntity : restaurantList) {
        if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
            servingRadiusInKms)) {
          restaurants.add(convertToDto(restaurantEntity));
        }
      }
      if(redisConfiguration.isCacheAvailable())
      saveRestaurantListToRedis(redisConfiguration.getJedisPool().getResource(), geoHash.toBase32(),
          restaurants);
    }


    // CHECKSTYLE:OFF
    // CHECKSTYLE:ON
    System.out.println("SIZE-->" + restaurants.size());
    return restaurants;
  }

  private static void saveRestaurantListToRedis(Jedis jedis, String key,
      List<Restaurant> restaurants) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String json = objectMapper.writeValueAsString(restaurants);
      jedis.set(key, json);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  private static List<Restaurant> retrieveRestaurantListFromRedis(Jedis jedis, String key) {
    ObjectMapper objectMapper = new ObjectMapper();
    List<Restaurant> restaurants = new ArrayList<>();
    String json = jedis.get(key);

    if (json != null) {
      try {
        restaurants = objectMapper.readValue(json, new TypeReference<List<Restaurant>>() {});
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return restaurants;
  }

  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude, restaurantEntity.getLatitude(),
          restaurantEntity.getLongitude()) < servingRadiusInKms;
    }
    return false;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose names have an exact or partial match with the search query.
  @Override
  public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurants = new ArrayList<>();
    List<RestaurantEntity> restaurantList =
        restaurantRepository.findRestaurantsByNameExact(searchString).get();
    for (RestaurantEntity restaurantEntity : restaurantList) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
          servingRadiusInKms)) {
        restaurants.add(convertToDto(restaurantEntity));
      }
    }
    return restaurants;
  }

  private Restaurant convertToDto(RestaurantEntity restaurantEntity) {
    return modelMapper.map(restaurantEntity, Restaurant.class);
  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose attributes (cuisines) intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurants = new ArrayList<>();
    List<RestaurantEntity> restaurantList =
        restaurantRepository.findByAttributesContaining(searchString).get();
    for (RestaurantEntity restaurantEntity : restaurantList) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
          servingRadiusInKms)) {
        restaurants.add(convertToDto(restaurantEntity));
      }
    }
    return restaurants;
  }



  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose names form a complete or partial match
  // with the search query.

  @Override
  public List<Restaurant> findRestaurantsByItemName(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {


    return null;
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose attributes intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

    return null;
  }
}

