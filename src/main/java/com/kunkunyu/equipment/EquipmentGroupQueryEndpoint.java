package com.kunkunyu.equipment;

import com.kunkunyu.equipment.finders.EquipmentPublicQueryService;
import com.kunkunyu.equipment.vo.EquipmentGroupVo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;


@Component
@RequiredArgsConstructor
public class EquipmentGroupQueryEndpoint implements CustomEndpoint {

    private final EquipmentPublicQueryService equipmentPublicQueryService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.equipment.hanserwei.github.io/v1alpha1/EquipmentGroup";
        return route()
            .GET("equipmentgroups", this::listGroups,
                builder -> {
                    builder.operationId("queryEquipmentGroups")
                        .description("List equipment groups.")
                        .tag(tag)
                        .response(responseBuilder().implementation(
                            ListResult.generateGenericClass(EquipmentGroupVo.class))
                        );
                }
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.equipment.hanserwei.github.io/v1alpha1");
    }

    private Mono<ServerResponse> listGroups(ServerRequest request) {
        EquipmentPublicQuery query = new EquipmentPublicQuery(request.exchange());
        return equipmentPublicQueryService.listGroups(query.toListOptions(), query.toPageRequest())
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result));
    }

}
