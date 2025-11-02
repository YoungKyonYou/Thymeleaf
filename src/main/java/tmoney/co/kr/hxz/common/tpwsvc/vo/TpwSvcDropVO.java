package tmoney.co.kr.hxz.common.tpwsvc.vo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TpwSvcDropVO {
    private TpwSvcVO tpwSvcVO;
    private List<TpwSvcTypVO> tpwSvcTypVOList;
}
