package com.kunkunyu.equipment.vo;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.theme.finders.vo.ExtensionVoOperator;
import com.kunkunyu.equipment.EquipmentGroup;


@Value
@Builder
public class EquipmentGroupVo implements ExtensionVoOperator {
    MetadataOperator metadata;
    
    EquipmentGroup.EquipmentGroupSpec spec;
    
    EquipmentGroup.EquipmentGroupStatus status;
    
    List<EquipmentVo> equipments;
    
    public static EquipmentGroupVo from(EquipmentGroup EquipmentGroup) {
        return EquipmentGroupVo.builder()
            .metadata(EquipmentGroup.getMetadata())
            .spec(EquipmentGroup.getSpec())
            .status(EquipmentGroup.getStatusOrDefault())
            .equipments(List.of())
            .build();
    }
}
