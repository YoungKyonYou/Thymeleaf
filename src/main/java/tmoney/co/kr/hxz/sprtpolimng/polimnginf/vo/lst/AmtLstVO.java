package tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.lst;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tmoney.co.kr.hxz.sprtpolimng.polimnginf.vo.amt.AmtReqVO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class AmtLstVO implements Serializable {
    List<AmtReqVO> list = new ArrayList<>();
}
