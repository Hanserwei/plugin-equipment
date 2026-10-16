package com.kunkunyu.equipment.finders.impl;

import com.kunkunyu.equipment.Equipment;
import com.kunkunyu.equipment.EquipmentGroup;
import com.kunkunyu.equipment.finders.EquipmentPublicQueryService;
import com.kunkunyu.equipment.vo.EquipmentGroupVo;
import com.kunkunyu.equipment.vo.EquipmentVo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;

import java.util.Comparator;

@Component
public class EquipmentPublicQueryServiceImpl implements EquipmentPublicQueryService {

    private final ReactiveExtensionClient client;

    public EquipmentPublicQueryServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<ListResult<EquipmentVo>> listEquipments(ListOptions options, PageRequest page) {

        return client.listBy(Equipment.class, options, page)
            .flatMap(result -> Flux.fromIterable(result.getItems())
                .map(EquipmentVo::from)
                .collectList()
                .map(items -> new ListResult<>(
                    result.getPage(), result.getSize(), result.getTotal(), items)));
    }

    @Override
    public Mono<ListResult<EquipmentGroupVo>> listGroups(ListOptions options, PageRequest page) {
        return client.listAll(EquipmentGroup.class, options, Sort.unsorted())
            .sort(groupComparator())
            .collectList()
            .flatMap(groups -> {
                int total = groups.size();
                var pageItems = ListResult.subList(groups, page.getPageNumber(), page.getPageSize());
                return Flux.fromIterable(pageItems)
                    .concatMap(this::toGroupVo)
                    .collectList()
                    .map(items -> new ListResult<>(page.getPageNumber(), page.getPageSize(), total, items));
            });
    }

    private Mono<EquipmentGroupVo> toGroupVo(EquipmentGroup group) {
        return fetchEquipmentCount(group)
            .map(count -> {
                var status = group.getStatusOrDefault();
                status.setEquipmentCount(count);
                return EquipmentGroupVo.builder()
                    .metadata(group.getMetadata())
                    .spec(group.getSpec())
                    .status(status)
                    .equipments(null)
                    .build();
            });
    }

    private Mono<Integer> fetchEquipmentCount(EquipmentGroup group) {
        String name = group.getMetadata().getName();
        return client.list(
                Equipment.class,
                equipment -> !equipment.isDeleted()
                    && equipment.getSpec() != null
                    && name.equals(equipment.getSpec().getGroupName()),
                null
            )
            .count()
            .defaultIfEmpty(0L)
            .map(Long::intValue);
    }

    static Comparator<EquipmentGroup> groupComparator() {
        return (g1, g2) -> {
            var p1 = g1.getSpec() != null && g1.getSpec().getPriority() != null
                ? g1.getSpec().getPriority() : 0;
            var p2 = g2.getSpec() != null && g2.getSpec().getPriority() != null
                ? g2.getSpec().getPriority() : 0;
            int priorityCompare = Integer.compare(p2, p1);
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            var t1 = g1.getMetadata() != null ? g1.getMetadata().getCreationTimestamp() : null;
            var t2 = g2.getMetadata() != null ? g2.getMetadata().getCreationTimestamp() : null;
            if (t1 == null && t2 == null) {
                return 0;
            }
            if (t1 == null) {
                return 1;
            }
            if (t2 == null) {
                return -1;
            }
            int timeCompare = t2.compareTo(t1);
            if (timeCompare != 0) {
                return timeCompare;
            }
            var n1 = g1.getMetadata() != null ? g1.getMetadata().getName() : "";
            var n2 = g2.getMetadata() != null ? g2.getMetadata().getName() : "";
            return n1.compareTo(n2);
        };
    }
}
