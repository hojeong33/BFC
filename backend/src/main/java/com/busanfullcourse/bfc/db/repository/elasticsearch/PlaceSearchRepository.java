package com.busanfullcourse.bfc.db.repository.elasticsearch;

import com.busanfullcourse.bfc.db.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


public interface PlaceSearchRepository extends ElasticsearchRepository<Place, Long>, NearPlaceSearchRepository {

    Page<Place> findByNameContains(String name, Pageable pageable);

    Page<Place> findAllByPlaceId(List<Long> ids, Pageable pageable);
}
