package com.kunkunyu.equipment;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author ryanwang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "equipment.hanserwei.github.io", version = "v1alpha1", kind = "Equipment", plural = "equipments",
    singular = "equipment")
public class Equipment extends AbstractExtension {

    private EquipmentSpec spec;

    @Data
    public static class EquipmentSpec {
        @Schema(requiredMode = REQUIRED)
        private String displayName;

        private String specification;

        private String description;

        private String cover;

        @Schema(description = "Device category; optional for existing equipment",
            allowableValues = {"desktop", "laptop", "phone", "tablet", "peripheral", "system", "other"})
        private String deviceType;

        @Schema(allowableValues = {"in-use", "standby", "retired"})
        private String status;

        @Schema(description = "Use a wide card for this equipment")
        private Boolean featured;

        @Schema(description = "Ordered, user-defined configuration entries")
        private List<Attribute> attributes;

        private String url;

        private Integer priority;

        @Schema(requiredMode = REQUIRED, pattern = "^\\S+$")
        private String groupName;
    }

    @Data
    public static class Attribute {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String label;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String value;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return Objects.equals(true,
            getMetadata().getDeletionTimestamp() != null
        );
    }

}
