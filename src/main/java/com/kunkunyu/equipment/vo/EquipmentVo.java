package com.kunkunyu.equipment.vo;

import java.net.URI;
import lombok.Builder;
import lombok.Value;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.theme.finders.vo.ExtensionVoOperator;
import com.kunkunyu.equipment.Equipment;


@Value
@Builder
public class EquipmentVo implements ExtensionVoOperator {
    
    MetadataOperator metadata;
    
    Equipment.EquipmentSpec spec;
    
    public String getDeviceTypeLabel() {
        return switch (spec.getDeviceType() == null ? "other" : spec.getDeviceType()) {
            case "desktop" -> "台式电脑";
            case "laptop" -> "笔记本电脑";
            case "phone" -> "手机";
            case "tablet" -> "平板电脑";
            case "peripheral" -> "外设";
            case "system" -> "操作系统";
            default -> "其他装备";
        };
    }

    public String getStatusLabel() {
        return switch (spec.getStatus() == null ? "" : spec.getStatus()) {
            case "in-use" -> "使用中";
            case "standby" -> "备用";
            case "retired" -> "已退役";
            default -> "";
        };
    }

    public String getLink() {
        if (spec.getUrl() == null || spec.getUrl().isBlank()) {
            return null;
        }
        String value = spec.getUrl().trim();
        try {
            URI uri = URI.create(value);
            boolean webUrl = ("https".equalsIgnoreCase(uri.getScheme())
                || "http".equalsIgnoreCase(uri.getScheme())) && uri.getHost() != null;
            boolean sitePath = uri.getScheme() == null && value.startsWith("/")
                && !value.startsWith("//");
            return webUrl || sitePath ? value : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static EquipmentVo from(Equipment equipment) {
        return EquipmentVo.builder()
            .metadata(equipment.getMetadata())
            .spec(equipment.getSpec())
            .build();
    }
}
