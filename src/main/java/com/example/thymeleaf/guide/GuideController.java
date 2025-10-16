package com.example.thymeleaf.guide;// src/main/java/com/example/guide/GuideController.java


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GuideController {

    @GetMapping("/page/guide/guide")
    public String guide(@RequestParam(required = false) String mngrId, Model model) throws Exception {

        // === 1) 하드코딩 데이터 (서비스 → 서비스유형들) ===
        List<SvcVm> svcList = mockServicesFor(mngrId);

        // map: tpwSvcId -> [{value: tpwSvcTypId, label: tpwSvcTypNm}, ...] 형태로 JSON 만들기
        Map<String, List<MapItem>> map = svcList.stream().collect(Collectors.toMap(
                SvcVm::getTpwSvcId,
                s -> s.getTypes().stream()
                        .map(t -> new MapItem(t.getTpwSvcTypId(), t.getTpwSvcTypNm()))
                        .collect(Collectors.toList())
        ));

        String svcTypeMapJson = new ObjectMapper().writeValueAsString(map);

        model.addAttribute("mngrId", mngrId);
        model.addAttribute("svcList", svcList);            // 1차 드롭다운용 (tpwSvcId / svcNm)
        model.addAttribute("svcTypeMapJson", svcTypeMapJson); // 2차 맵핑 JSON

        // 화면: /templates/page/guide/guide.html
        return "page/guide/guide";
    }

    private List<SvcVm> mockServicesFor(String mngrId) {
        // 관리자에 따라 다르게 주는 척만—하드코딩
        // tpwSvcId / svcNm / (tpwSvcTypId, tpwSvcTypNm)...
        SvcVm s1 = new SvcVm("SVC001", "교통비 지원",
                Arrays.asList(
                        new SvcTypVm("TYP001", "청소년 교통비"),
                        new SvcTypVm("TYP002", "어르신 교통비")
                ));
        SvcVm s2 = new SvcVm("SVC002", "육아지원",
                Arrays.asList(
                        new SvcTypVm("TYP010", "기저귀 바우처"),
                        new SvcTypVm("TYP011", "분유 바우처")
                ));
        SvcVm s3 = new SvcVm("SVC003", "창업지원",
                Arrays.asList(
                        new SvcTypVm("TYP020", "임대료 지원"),
                        new SvcTypVm("TYP021", "컨설팅 지원"),
                        new SvcTypVm("TYP022", "시제품 지원")
                ));

        if (mngrId == null || mngrId.isEmpty()) {
            return Arrays.asList(s1, s2, s3);
        }
        // mngrId 에 따라 일부만 보이게 하는 척
        if (mngrId.startsWith("A")) return Arrays.asList(s1, s3);
        if (mngrId.startsWith("B")) return Arrays.asList(s2);
        return Arrays.asList(s1, s2, s3);
    }

    // ====== 뷰 모델들 ======
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SvcTypVm {
        private String tpwSvcTypId;   // 서비스유형ID
        private String tpwSvcTypNm;   // 서비스유형명
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class SvcVm {
        private String tpwSvcId;      // 서비스ID
        private String svcNm;         // 서비스명
        private List<SvcTypVm> types; // 하위 유형들
    }

    // JSON으로 내려줄 단순 아이템(value/label)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class MapItem {
        private String value;
        private String label;
    }
}
