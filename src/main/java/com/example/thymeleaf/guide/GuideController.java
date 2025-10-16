// src/main/java/com/example/thymeleaf/guide/GuideController.java
package com.example.thymeleaf.guide;

import lombok.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GuideController {

    // 페이지(뷰) 렌더
    @GetMapping("/page/guide/guide")
    public String guide(@RequestParam(required = false) String mngrId, Model model) {
        model.addAttribute("mngrId", mngrId);
        return "page/guide/guide"; // /templates/page/guide/guide.html
    }

    // ✅ 단일 API: 서비스 목록 + 유형 맵 (JSON)
    @GetMapping("/api/services")
    @ResponseBody
    public AllDataResponse services(@RequestParam(required = false) String mngrId) {
        List<SvcVm> all = mockServicesFor(mngrId);

        List<SvcDto> services = all.stream()
                .map(s -> new SvcDto(s.getTpwSvcId(), s.getSvcNm()))
                .collect(Collectors.toList());

        Map<String, List<SvcTypeDto>> typesBySvcId = all.stream().collect(Collectors.toMap(
                SvcVm::getTpwSvcId,
                s -> s.getTypes().stream()
                        .map(t -> new SvcTypeDto(t.getTpwSvcTypId(), t.getTpwSvcTypNm()))
                        .collect(Collectors.toList())
        ));

        return new AllDataResponse(services, typesBySvcId);
    }

    // ===== Mock 데이터 =====
    private List<SvcVm> mockServicesFor(String mngrId) {
        SvcVm s1 = new SvcVm("SVC001", "교통비 지원",
                Arrays.asList(
                        new SvcTypVm("TYP001","청소년 교통비"),
                        new SvcTypVm("TYP002","어르신 교통비")
                ));
        SvcVm s2 = new SvcVm("SVC002", "육아지원",
                Arrays.asList(
                        new SvcTypVm("TYP010","기저귀 바우처"),
                        new SvcTypVm("TYP011","분유 바우처")
                ));
        SvcVm s3 = new SvcVm("SVC003", "창업지원",
                Arrays.asList(
                        new SvcTypVm("TYP020","임대료 지원"),
                        new SvcTypVm("TYP021","컨설팅 지원"),
                        new SvcTypVm("TYP022","시제품 지원")
                ));

        if (mngrId == null || mngrId.isEmpty()) return Arrays.asList(s1, s2, s3);
        if (mngrId.startsWith("A")) return Arrays.asList(s1, s3);
        if (mngrId.startsWith("B")) return Arrays.asList(s2);
        return Arrays.asList(s1, s2, s3);
    }

    // ===== DTO/VM =====
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SvcTypVm { private String tpwSvcTypId; private String tpwSvcTypNm; }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SvcVm { private String tpwSvcId; private String svcNm; private List<SvcTypVm> types; }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SvcDto { private String tpwSvcId; private String svcNm; }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SvcTypeDto { private String tpwSvcTypId; private String tpwSvcTypNm; }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AllDataResponse {
        private List<SvcDto> services;                        // 1차
        private Map<String, List<SvcTypeDto>> typesBySvcId;   // 2차 맵
    }
}
