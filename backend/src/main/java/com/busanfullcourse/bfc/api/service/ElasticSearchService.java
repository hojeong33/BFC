package com.busanfullcourse.bfc.api.service;

import com.busanfullcourse.bfc.api.response.SearchPlaceListRes;
import com.busanfullcourse.bfc.db.entity.CustomPlace;
import com.busanfullcourse.bfc.db.entity.Place;
import com.busanfullcourse.bfc.db.entity.Schedule;
import com.busanfullcourse.bfc.db.repository.PlaceRepository;
import com.busanfullcourse.bfc.db.repository.ScheduleRepository;
import com.busanfullcourse.bfc.db.repository.elasticsearch.PlaceSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ElasticSearchService {

    private final PlaceSearchRepository placeSearchRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleRepository scheduleRepository;

    public Page<SearchPlaceListRes> searchPlaceByName(String name, Pageable pageable) {
        Page<Place> list = placeSearchRepository.findByNameContains(name, pageable);
        return SearchPlaceListRes.of(list);
    }

    public Page<Place> searchAll(Pageable pageable) {
        return placeSearchRepository.findAll(pageable);
    }

    public void saveAll() {
        placeSearchRepository.saveAll(placeRepository.findAll().stream()
                .peek(place -> place.setLocation(new GeoPoint(place.getLat(), place.getLon())))
                .collect(Collectors.toList()));
    }

    public void deleteAll() {
        placeSearchRepository.deleteAll();
    }

    public Page<SearchPlaceListRes> searchByDistance(Long scheduleId, Integer distance, Pageable pageable) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(NoSuchElementException::new);
        Place place = schedule.getPlace();
        Page<Place> list;

        if (place == null) {
            CustomPlace customPlace = schedule.getCustomPlace();
            list = placeSearchRepository.searchByGeoPointAndDistance(customPlace.getLat(), customPlace.getLon(), distance, pageable);
        } else {
            list = placeSearchRepository.searchByGeoPointAndDistance(place.getLat(), place.getLon(), distance, pageable);
        }

        if (place != null) {
            Iterator<Place> iterator = list.iterator();
            while (iterator.hasNext()) {
                Place now = iterator.next();
                if (Objects.equals(now.getPlaceId(), place.getPlaceId())) {
                    iterator.remove();
                    break;
                }
            }
        }
        return SearchPlaceListRes.of(list);
    }
}