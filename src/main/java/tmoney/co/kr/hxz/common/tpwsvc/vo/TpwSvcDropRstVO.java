package tmoney.co.kr.hxz.common.tpwsvc.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TpwSvcDropRstVO {
    private List<TpwSvcVO> services;
    private Map<String, List<TpwSvcInfVO>> typesBySvcId;
}