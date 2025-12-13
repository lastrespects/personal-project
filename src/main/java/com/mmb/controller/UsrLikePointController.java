// UsrLikePointController.java
package com.mmb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmb.dto.LikePoint;
import com.mmb.dto.Req;
import com.mmb.dto.ResultData;
import com.mmb.service.LikePointService;

@Controller
public class UsrLikePointController {

    private LikePointService likePointService;
    private Req req;

    public UsrLikePointController(LikePointService likePointService, Req req) {
        this.likePointService = likePointService;
        this.req = req;
    }

    @GetMapping("/usr/likePoint/getLikePoint")
    @ResponseBody
    public ResultData<Integer> getLikePoint(String relTypeCode, int relId) {

        LikePoint likePoint = this.likePointService.getLikePoint(
                Math.toIntExact(this.req.getLoginedMember().getId()),
                relTypeCode,
                relId);
        int likePointCnt = this.likePointService.getLikePointCnt(relTypeCode, relId);

        if (likePoint == null) {
            return new ResultData<>("F-1", "LIKE_NOT_FOUND", likePointCnt);
        }

        return new ResultData<>("S-1", "LIKE_FOUND", likePointCnt);
    }

    @GetMapping("/usr/likePoint/clickLikePoint")
    @ResponseBody
    public String clickLikePoint(String relTypeCode, int relId, boolean likePointBtn) {

        if (!likePointBtn) {
            this.likePointService.deleteLikePoint(
                    Math.toIntExact(this.req.getLoginedMember().getId()),
                    relTypeCode,
                    relId);
            return "LIKE_CANCEL";
        }

        this.likePointService.insertLikePoint(
                Math.toIntExact(this.req.getLoginedMember().getId()),
                relTypeCode,
                relId);

        return "LIKE_ADD";
    }
}
