package com.kunkunyu.equipment;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.kunkunyu.equipment.finders.EquipmentPublicQueryService;
import com.kunkunyu.equipment.vo.EquipmentVo;
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
/**
 * Public endpoint for equipment queries.
 */
@Component
@RequiredArgsConstructor
public class EquipmentQueryEndpoint implements CustomEndpoint {

    private final EquipmentPublicQueryService equipmentPublicQueryService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.equipment.hanserwei.github.io/v1alpha1/Equipment";
        return route()
            .GET("equipments", this::listEquipments,
                builder -> {
                    builder.operationId("queryEquipments")
                        .description("List equipments.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(EquipmentVo.class)));
                    EquipmentPublicQuery.buildParameters(builder);
                }
            )
            .build();
    }

    private Mono<ServerResponse> listEquipments(ServerRequest request) {
        EquipmentPublicQuery query = new EquipmentPublicQuery(request.exchange());
        return equipmentPublicQueryService.listEquipments(query.toListOptions(), query.toPageRequest())
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result));
    }
    
    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.equipment.hanserwei.github.io/v1alpha1");
    }
}
