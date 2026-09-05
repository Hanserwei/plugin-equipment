package com.kunkunyu.equipment;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunkunyu.equipment.vo.EquipmentGroupVo;
import com.kunkunyu.equipment.vo.EquipmentVo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class EquipmentTemplateTest {
    private final ObjectMapper mapper = new ObjectMapper();

    private EquipmentVo desktop() throws Exception {
        Equipment equipment = mapper.readValue(Path.of("docs/examples/desktop.json").toFile(), Equipment.class);
        return EquipmentVo.from(equipment);
    }

    private EquipmentGroupVo group(List<EquipmentVo> equipments) {
        var spec = new EquipmentGroup.EquipmentGroupSpec();
        spec.setDisplayName("日常设备");
        spec.setDescription("记录每天使用的设备与配置。");
        return EquipmentGroupVo.builder().spec(spec).equipments(equipments).build();
    }

    private String render(List<EquipmentGroupVo> groups) {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateAliases(Map.of(
            "plugin:equipment:modules/equipment", "modules/equipment",
            "plugin:equipment:modules/card", "modules/card",
            "plugin:equipment:modules/icon", "modules/icon"
        ));
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var context = new Context();
        context.setVariable("groups", groups);
        return engine.process("modules/equipment", context);
    }

    @Test
    void rendersAllConfigurationFieldsAndAnIconWithoutACover() throws Exception {
        var equipment = desktop();
        var document = Jsoup.parse(render(List.of(group(List.of(equipment)))));
        assertThat(document.select(".equipment-card--featured")).hasSize(1);
        assertThat(document.select(".equipment-card-spec")).hasSize(8);
        assertThat(document.select(".equipment-device-icon")).hasSize(1);
        assertThat(document.select("img")).isEmpty();
        assertThat(document.select(".equipment-card-type").text()).isEqualTo("台式电脑");
        assertThat(document.select(".equipment-card-status").text()).isEqualTo("使用中");
        assertThat(document.text()).contains("i9-14900K", "RTX 3060", "Arch Linux", "niri 26.04");
        assertThat(document.select(".equipment-empty")).isEmpty();
    }

    @Test
    void escapesParameterTextAndOmitsExecutableLinks() throws Exception {
        var equipment = desktop();
        equipment.getSpec().getAttributes().getFirst().setValue("<script>alert(1)</script>");
        equipment.getSpec().setUrl("javascript:alert(1)");
        var document = Jsoup.parse(render(List.of(group(List.of(equipment)))));
        assertThat(document.select("script, .equipment-card-link")).isEmpty();
        assertThat(document.select("dd").first().text()).isEqualTo("<script>alert(1)</script>");
    }

    @Test
    void rendersOptionalImagesLinksAndEmptyGroups() throws Exception {
        var equipment = desktop();
        equipment.getSpec().setCover("https://example.com/device.png");
        equipment.getSpec().setUrl("/archives/my-desktop");
        equipment.getSpec().setAttributes(List.of());
        equipment.getSpec().setFeatured(false);
        var document = Jsoup.parse(render(List.of(group(List.of(equipment)))));
        assertThat(document.select("img").attr("loading")).isEqualTo("lazy");
        assertThat(document.select(".equipment-card-specs, .equipment-card--featured")).isEmpty();
        assertThat(document.select("a").attr("rel")).contains("noopener", "noreferrer");
        assertThat(Jsoup.parse(render(List.of())).select(".equipment-empty")).hasSize(1);
        assertThat(Jsoup.parse(render(List.of(group(List.of())))).select(".equipment-empty")).hasSize(1);
        for (String url : List.of("javascript:alert(1)", "data:text/html,test", "//example.com", "https://", "bad url")) {
            equipment.getSpec().setUrl(url);
            assertThat(equipment.getLink()).isNull();
        }
    }

    @Test
    void createsPreviewFromRealTemplates() throws Exception {
        var phone = new Equipment.EquipmentSpec();
        phone.setDisplayName("随身手机（示例）");
        phone.setDeviceType("phone");
        phone.setStatus("in-use");
        phone.setSpecification("通讯、影像与生活记录");
        phone.setAttributes(List.of(attribute("型号", "填写你的手机型号"), attribute("操作系统", "填写系统与版本")));
        var system = new Equipment.EquipmentSpec();
        system.setDisplayName("Arch Linux");
        system.setDeviceType("system");
        system.setStatus("in-use");
        system.setSpecification("我的桌面环境");
        system.setAttributes(List.of(attribute("更新方式", "Rolling Release"), attribute("窗口管理器", "niri 26.04"), attribute("窗口系统", "Wayland")));
        var laptop = new Equipment.EquipmentSpec();
        laptop.setDisplayName("移动工作站（示例）");
        laptop.setDeviceType("laptop");
        laptop.setStatus("standby");
        laptop.setSpecification("出门时的轻装搭档");
        laptop.setAttributes(List.of(attribute("型号", "填写你的笔记本型号"), attribute("内存 / 存储", "按实际配置填写")));
        String cards = render(List.of(group(List.of(desktop(), EquipmentVo.builder().spec(phone).build(),
            EquipmentVo.builder().spec(system).build(), EquipmentVo.builder().spec(laptop).build()))));
        Document preview = Jsoup.parse("<!doctype html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><title>装备展示预览</title>"
            + "</head><body class=\"equipment-page\"><main class=\"equipment-page-main\">"
            + "<header class=\"equipment-page-header\"><p>MY EVERYDAY GEAR</p><h1>我的装备</h1>"
            + "<div>陪伴日常的设备，以及它们背后的配置。</div></header>" + cards + "</main></body></html>");
        preview.head().appendElement("style").text(Files.readString(Path.of("src/main/resources/static/equipment.css")));
        Path output = Path.of("build/reports/equipment-preview.html");
        Files.createDirectories(output.getParent());
        Files.writeString(output, preview.outerHtml());
        assertThat(preview.select(".equipment-card")).hasSize(4);
    }

    private Equipment.Attribute attribute(String label, String value) {
        var attribute = new Equipment.Attribute();
        attribute.setLabel(label);
        attribute.setValue(value);
        return attribute;
    }
}
