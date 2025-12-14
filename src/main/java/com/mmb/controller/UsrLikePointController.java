package com.mmb.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmb.dto.LikePoint;
import com.mmb.dto.Req;
import com.mmb.dto.ResultData;
import com.mmb.service.LikePointService;
import com.mmb.service.LikePointWriteService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class UsrLikePointController {

    private final LikePointService likePointService;
    private final LikePointWriteService likePointWriteService;
    private final Req req;

    public UsrLikePointController(LikePointService likePointService, LikePointWriteService likePointWriteService, Req req) {
        this.likePointService = likePointService;
        this.likePointWriteService = likePointWriteService;
        this.req = req;
    }

    @PostMapping("/usr/likePoint/toggle")
    @ResponseBody
    public ResultData<Map<String, Object>> toggle(@RequestParam String relTypeCode,
                                                  @RequestParam(required = false) Integer relId) {
        Integer memberId = req.getLoginedMemberId();
        if (memberId == null) {
            return ResultData.from("F-401", "로그인이 필요합니다.");
        }

        if (!StringUtils.hasText(relTypeCode)) {
            return ResultData.from("F-400", "relTypeCode가 없습니다.");
        }
        if (relId == null || relId <= 0) {
            return ResultData.from("F-400", "relId가 없습니다.");
        }

        String normalizedRelType = relTypeCode.trim();
        try {
            LikePoint likePoint = likePointService.getLikePoint(memberId, normalizedRelType, relId);
            boolean liked;
            if (likePoint != null) {
                likePointWriteService.deleteLikePoint(memberId, normalizedRelType, relId);
                liked = false;
            } else {
                likePointWriteService.insertLikePoint(memberId, normalizedRelType, relId);
                liked = true;
            }

            int likeCount = likePointService.getLikePointCnt(normalizedRelType, relId);
            return ResultData.from("S-1", "OK", Map.of(
                    "liked", liked,
                    "likeCount", likeCount
            ));
        } catch (Exception e) {
            log.error("[WRITE_FAIL] endpoint=/usr/likePoint/toggle memberId={}, relTypeCode={}, relId={}",
                    memberId, normalizedRelType, relId, e);
            return ResultData.from("F-500", "좋아요 처리 중 오류가 발생했습니다.");
        }
    }
}
