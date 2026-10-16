package com.kunkunyu.equipment.finders;

import com.kunkunyu.equipment.vo.EquipmentGroupVo;
import com.kunkunyu.equipment.vo.EquipmentVo;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;

public interface EquipmentPublicQueryService {

    /**
     * List equipments with filters and pagination.
     *
     * @param options list options
     * @param page    page request
     * @return a mono of list result
     */
    Mono<ListResult<EquipmentVo>> listEquipments(ListOptions options, PageRequest page);

    /**
     * List photo groups without inline photos.
     *
     * @param options list options
     * @param page    page request
     * @return a mono of list result
     */
    Mono<ListResult<EquipmentGroupVo>> listGroups(ListOptions options, PageRequest page);

}
