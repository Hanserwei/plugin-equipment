package com.kunkunyu.equipment;

import static org.assertj.core.api.Assertions.assertThat;

import com.kunkunyu.equipment.finders.impl.EquipmentFInderImpl;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersion;
import run.halo.app.theme.finders.Finder;

class PluginIdentityTest {
    private static final String GROUP = "equipment.hanserwei.github.io";

    @Test
    void registersModelsAndEndpointsInTheIndependentNamespace() {
        assertThat(Equipment.class.getAnnotation(GVK.class).group()).isEqualTo(GROUP);
        assertThat(EquipmentGroup.class.getAnnotation(GVK.class).group()).isEqualTo(GROUP);
        var console = GroupVersion.parseAPIVersion("console.api." + GROUP + "/v1alpha1");
        var publicApi = GroupVersion.parseAPIVersion("api." + GROUP + "/v1alpha1");
        assertThat(new EquipmentEndpoint(null).groupVersion()).isEqualTo(console);
        assertThat(new EquipmentGroupEndpoint(null).groupVersion()).isEqualTo(console);
        assertThat(new EquipmentQueryEndpoint(null).groupVersion()).isEqualTo(publicApi);
        assertThat(new EquipmentGroupQueryEndpoint(null).groupVersion()).isEqualTo(publicApi);
        assertThat(EquipmentFInderImpl.class.getAnnotation(Finder.class).value()).isEqualTo("hanEquipmentFinder");
    }

    @Test
    @SuppressWarnings("unchecked")
    void settingsAndRolesBelongToTheRenamedPlugin() throws IOException {
        var plugin = resource("plugin.yaml");
        var spec = (Map<String, Object>) plugin.get("spec");
        assertThat(metadata(plugin).get("name")).isEqualTo("han-equipment");
        assertThat(spec.get("displayName")).isEqualTo("han-equipment");
        assertThat(spec.get("settingName")).isEqualTo(metadata(resource("extensions/settings.yaml")).get("name"));
        assertThat(spec.get("configMapName")).isEqualTo("han-equipment-configmap");
        assertThat(metadata(resource("extensions/reverseProxy.yaml")).get("name")).isEqualTo("han-equipment-static-assets");

        try (var stream = getClass().getClassLoader().getResourceAsStream("extensions/roleTemplate.yaml")) {
            assertThat(stream).isNotNull();
            for (Object value : new Yaml().loadAll(stream)) {
                var role = (Map<String, Object>) value;
                assertThat(metadata(role).get("name").toString()).contains("han-equipment");
                for (var rule : (List<Map<String, Object>>) role.get("rules")) {
                    assertThat((List<String>) rule.get("apiGroups"))
                        .isNotEmpty().isSubsetOf(GROUP, "console.api." + GROUP, "api." + GROUP);
                    if (((List<String>) rule.get("apiGroups")).contains("api." + GROUP)) {
                        assertThat((List<String>) rule.get("verbs")).containsExactly("get", "list");
                    }
                }
            }
        }
    }

    private Map<String, Object> resource(String path) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(stream).isNotNull();
            return new Yaml().load(stream);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> metadata(Map<String, Object> value) {
        return (Map<String, Object>) value.get("metadata");
    }
}
