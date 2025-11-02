package tmoney.co.kr.hxz.common.tpwsvc.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tmoney.co.kr.hxz.common.tpwsvc.service.TpwSvcService;
import tmoney.co.kr.hxz.common.tpwsvc.vo.TpwSvcDropRstVO;
import tmoney.co.kr.hxz.common.tpwsvc.vo.TpwSvcInfVO;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/common/tpwsvc")
public class TpwSvcController {
    private final TpwSvcService tpwSvcService;

    @GetMapping("/tpwSvcInf")
    public TpwSvcDropRstVO findTpwSvcInf() {
        List<TpwSvcInfVO> tpwSvcInfVOS = tpwSvcService.readTpwSvcInfList("0000000");
        return tpwSvcService.processTpwSvcInf(tpwSvcInfVOS);
    }
}
