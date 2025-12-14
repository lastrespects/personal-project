package com.mmb.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmb.dto.Reply;
import com.mmb.dto.Req;
import com.mmb.dto.ResultData;
import com.mmb.service.ReplyService;
import com.mmb.service.ReplyWriteService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class UsrReplyController {

    private final ReplyService replyService;
    private final ReplyWriteService replyWriteService;
    private final Req req;

    public UsrReplyController(ReplyService replyService, ReplyWriteService replyWriteService, Req req) {
        this.replyService = replyService;
        this.replyWriteService = replyWriteService;
        this.req = req;
    }

    @GetMapping("/usr/reply/list")
    @ResponseBody
    public ResultData<List<Reply>> list(@RequestParam String relTypeCode,
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

        try {
            List<Reply> replies = this.replyService.getReplies(relTypeCode.trim(), relId);
            return ResultData.from("S-1", "OK", replies);
        } catch (Exception e) {
            log.error("[WRITE_FAIL] endpoint=/usr/reply/list memberId={}, relTypeCode={}, relId={}",
                    memberId, relTypeCode, relId, e);
            return ResultData.from("F-500", "댓글 목록을 불러오지 못했습니다.");
        }
    }

    @PostMapping("/usr/reply/write")
    @ResponseBody
    public ResultData<Reply> write(@RequestParam String relTypeCode,
                                   @RequestParam(required = false) Integer relId,
                                   @RequestParam String content) {
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
        if (!StringUtils.hasText(content)) {
            return ResultData.from("F-400", "내용을 입력해 주세요.");
        }

        String normalizedRelType = relTypeCode.trim();
        String body = content.trim();
        try {
            int replyId = this.replyWriteService.writeReply(memberId, normalizedRelType, relId, body);
            Reply reply = this.replyService.getReply(replyId);
            return ResultData.from("S-1", "댓글이 등록되었습니다.", reply);
        } catch (Exception e) {
            log.error("[WRITE_FAIL] endpoint=/usr/reply/write memberId={}, relTypeCode={}, relId={}",
                    memberId, normalizedRelType, relId, e);
            return ResultData.from("F-500", "댓글 등록 처리 중 문제가 발생했습니다.");
        }
    }

    @PostMapping("/usr/reply/delete")
    @ResponseBody
    public ResultData<Void> delete(@RequestParam(required = false) Integer id) {
        Integer memberId = req.getLoginedMemberId();
        if (memberId == null) {
            return ResultData.from("F-401", "로그인이 필요합니다.");
        }
        if (id == null || id <= 0) {
            return ResultData.from("F-400", "댓글 id가 없습니다.");
        }

        Reply reply = this.replyService.getReply(id);
        if (reply == null) {
            return ResultData.from("F-404", "댓글을 찾을 수 없습니다.");
        }

        boolean isAdmin = req.isAdmin();
        Integer writerId = reply.getMemberId();
        boolean isOwner = (writerId != null && writerId.equals(memberId));

        // ✅ 본인 댓글 OR 관리자만 삭제 가능
        if (!isOwner && !isAdmin) {
            return ResultData.from("F-403", "본인 댓글만 삭제할 수 있습니다.");
        }

        try {
            this.replyWriteService.deleteReply(id);
            return ResultData.from("S-1", "댓글이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("[WRITE_FAIL] endpoint=/usr/reply/delete memberId={}, replyId={}", memberId, id, e);
            return ResultData.from("F-500", "댓글 삭제 처리 중 문제가 발생했습니다.");
        }
    }
}
