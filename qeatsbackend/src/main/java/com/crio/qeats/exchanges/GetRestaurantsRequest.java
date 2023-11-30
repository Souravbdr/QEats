/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
// Implement GetRestaurantsRequest.
// Complete the class such that it is able to deserialize the incoming query params from
// REST API clients.
// For instance, if a REST client calls API
// /qeats/v1/restaurants?latitude=28.4900591&longitude=77.536386&searchFor=tamil,
// this class should be able to deserialize lat/long and optional searchFor from that.
@Getter
@Data
@RequiredArgsConstructor
public class GetRestaurantsRequest {

    @NotNull(message = "missing latitude")
    @DecimalMin(value = "-90", inclusive = true, message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90", inclusive = true, message = "Latitude must be less than or equal to 90")
    private final Double latitude;

    @NotNull(message = "missing longitude")
    @DecimalMin(value = "-180", inclusive = true, message = "Longitude must be greater than or equal to -180")
    @DecimalMax(value = "180", inclusive = true, message = "Longitude must be less than or equal to 180")
    private final Double longitude;

    @JsonProperty(required=false)
    private String searchFor;

}

