package com.kunkunyu.equipment;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.contains;
import static run.halo.app.extension.index.query.Queries.empty;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.index.query.Condition;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.SortableRequest;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * A query object for public {@link Equipment} list.
 */
public class EquipmentPublicQuery extends SortableRequest {

    public EquipmentPublicQuery(ServerWebExchange exchange) {
        super(exchange);
    }

    public String getGroup() {
        return queryParams.getFirst("group");
    }

    @Nullable
    public String getKeyword() {
        return queryParams.getFirst("keyword");
    }

    /**
     * Build {@link ListOptions} from query params.
     *
     * @return a list options.
     */
    public ListOptions toListOptions() {
        var listOptions =
            labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());
        Condition query = empty();
        if (StringUtils.isNotBlank(getKeyword())) {
            query = and(query, contains("spec.displayName", getKeyword()));
        }
        if (StringUtils.isNotBlank(getGroup())) {
            query = and(query, equal("spec.groupName", getGroup()));
        }
        if (listOptions.getFieldSelector() != null) {
            query = and(query, (Condition) listOptions.getFieldSelector().query());
        }
        listOptions.setFieldSelector(FieldSelector.of(query));
        return listOptions;
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("Equipments filtered by keyword.")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("group")
                .description("equipment group name")
                .implementation(String.class)
                .required(false));
    }
}