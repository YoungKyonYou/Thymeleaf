package tmoney.co.kr.hxz.common.cmncd.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tmoney.co.kr.hxz.common.cmncd.mapper.CmnCdMapper;
import tmoney.co.kr.hxz.common.cmncd.vo.CmnCdVO;

@RequiredArgsConstructor
@Service
public class CmnCdService {
    private final CmnCdMapper cmnCdMapper;

    @Transactional(readOnly = true)
    public List<CmnCdVO> readCmnCdInf(String cmnGrpCdId) {
        return cmnCdMapper.readCmnCdInf(cmnGrpCdId);
    }
}
