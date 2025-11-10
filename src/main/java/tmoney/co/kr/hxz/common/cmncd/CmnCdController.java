package tmoney.co.kr.hxz.common.cmncd;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tmoney.co.kr.hxz.common.cmncd.service.CmnCdService;
import tmoney.co.kr.hxz.common.cmncd.vo.CmnCdVO;

@RequiredArgsConstructor
@RestController
public class CmnCdController {
    private final CmnCdService cmnCdService;

    @GetMapping("/common/cmncd")
    public List<CmnCdVO> getCmnCdPage(@RequestParam("cmnGrpCdId") String cmnGrpCdId, Model model) {
        return cmnCdService.readCmnCdInf(cmnGrpCdId);
    }
}
