package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.ncnt.NcntReqVO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class NcntLstVO implements Serializable {
    List<NcntReqVO> list = new ArrayList<>();

    private String lmtSttYm = "";
}
