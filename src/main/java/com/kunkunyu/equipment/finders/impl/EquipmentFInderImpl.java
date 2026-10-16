package com.kunkunyu.equipment.finders.impl;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;

import com.kunkunyu.equipment.finders.EquipmentPublicQueryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.theme.finders.Finder;
import com.kunkunyu.equipment.finders.EquipmentFinder;
import com.kunkunyu.equipment.vo.EquipmentGroupVo;
import com.kunkunyu.equipment.vo.EquipmentVo;

@Finder("hanEquipmentFinder")
public class EquipmentFInderImpl implements EquipmentFinder {
    private final ReactiveExtensionClient client;

    private final EquipmentPublicQueryService equipmentPublicQueryService;

    public EquipmentFInderImpl(ReactiveExtensionClient client,
        EquipmentPublicQueryService equipmentPublicQueryService) {
        this.client = client;
        this.equipmentPublicQueryService = equipmentPublicQueryService;
    }

    @Override
    public Flux<EquipmentVo> listAll() {
        return equipmentPublicQueryService.listEquipments(ListOptions.builder().build(),
                PageRequestImpl.of(1, Integer.MAX_VALUE, defaultSort()))
            .flatMapIterable(ListResult::getItems);
    }

    @Override
    public Mono<ListResult<EquipmentVo>> list(Integer page, Integer size) {
        return list(page, size, null);
    }

    @Override
    public Mono<ListResult<EquipmentVo>> list(Integer page, Integer size, String group) {
        return pageEquipment(page, size, group);
    }

    private Mono<ListResult<EquipmentVo>> pageEquipment(Integer page, Integer size, String group) {
        var options = ListOptions.builder();
        if (StringUtils.isNotEmpty(group)) {
            options.andQuery(Queries.equal("spec.groupName", group));
        }

        return equipmentPublicQueryService.listEquipments(options.build(),
            PageRequestImpl.of(page, size, defaultSort()));
    }

    @Override
    public Flux<EquipmentVo> listBy(String groupName) {
        var options = ListOptions.builder()
            .andQuery(Queries.equal("spec.groupName", groupName))
            .build();
        return equipmentPublicQueryService.listEquipments(options,
                PageRequestImpl.of(1, Integer.MAX_VALUE, defaultSort()))
            .flatMapIterable(ListResult::getItems);
    }

    @Override
    public Flux<EquipmentGroupVo> groupBy() {

        return equipmentPublicQueryService.listGroups(ListOptions.builder().build(),
                PageRequestImpl.of(1, Integer.MAX_VALUE, defaultSort()))
            .flatMapIterable(ListResult::getItems)
            .concatMap(group -> {
                String groupName = group.getMetadata().getName();
                return equipmentPublicQueryService.listEquipments(
                        ListOptions.builder()
                            .andQuery(Queries.equal("spec.groupName", groupName))
                            .build(),
                        PageRequestImpl.of(1, Integer.MAX_VALUE, defaultSort()))
                    .map(equipments -> {
                        var status = group.getStatus();
                        return EquipmentGroupVo.builder()
                            .metadata(group.getMetadata())
                            .spec(group.getSpec())
                            .status(status)
                            .equipments(equipments.getItems())
                            .build();
                    });
            });
    }

    static Sort defaultSort() {
        return Sort.by(
            asc("spec.priority"),
            desc("metadata.creationTimestamp"),
            asc("metadata.name")
        );
    }
}
