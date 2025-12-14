package com.mmb.controller;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.mmb.dto.Article;
import com.mmb.dto.Reply;
import com.mmb.dto.Req;
import com.mmb.entity.Member;
import com.mmb.service.ArticleService;
import com.mmb.service.LikePointService;
import com.mmb.service.MemberService;
import com.mmb.service.ReplyService;
import com.mmb.util.BoardType;
import com.mmb.util.Util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UsrArticleController {

    private final ArticleService articleService;
    private final ReplyService replyService;
    private final LikePointService likePointService;
    private final Req req;
    private final MemberService memberService;

    public UsrArticleController(ArticleService articleService,
                                ReplyService replyService,
                                LikePointService likePointService,
                                Req req,
                                MemberService memberService) {
        this.articleService = articleService;
        this.replyService = replyService;
        this.likePointService = likePointService;
        this.req = req;
        this.memberService = memberService;
    }

    private BoardType requireBoardType(int boardId) {
        return BoardType.fromId(boardId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "게시판을 찾을 수 없습니다."));
    }

    private boolean isNoticeBoard(BoardType boardType) {
        return boardType == BoardType.NOTICE;
    }

    private boolean isAdmin() {
        Member member = resolveCurrentMember();
        return member != null && member.getAuthLevel() == 0;
    }

    private Member resolveCurrentMember() {
        Integer loginedMemberId = req.getLoginedMemberId();
        if (req.getLoginedMember() != null && loginedMemberId != null) {
            return memberService.findById(loginedMemberId).orElse(null);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return memberService.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping("/usr/article/write")
    public String write(Model model, int boardId) {
        BoardType boardType = requireBoardType(boardId);
        if (isNoticeBoard(boardType) && !isAdmin()) {
            req.jsPrintReplace("공지사항은 관리자만 작성할 수 있습니다.", "/usr/article/list?boardId=" + boardType.getId());
            return null;
        }
        model.addAttribute("boardId", boardType.getId());
        model.addAttribute("boardName", boardType.getDisplayName());
        return "usr/article/write";
    }

    @PostMapping("/usr/article/doWrite")
    @ResponseBody
    public String doWrite(String title, String content, int boardId) {
        BoardType boardType = requireBoardType(boardId);
        if (isNoticeBoard(boardType) && !isAdmin()) {
            return Util.jsReplace("공지사항은 관리자만 작성할 수 있습니다.", "/usr/article/list?boardId=" + boardType.getId());
        }

        Member member = resolveCurrentMember();
        if (member == null) {
            return Util.jsReplace("로그인 후 이용해주세요.", "/login");
        }

        this.articleService.writeArticle(title, content, member.getId(), boardType.getId());
        int id = this.articleService.getLastInsertId();

        return Util.jsReplace("게시글이 등록되었습니다.", String.format("detail?id=%d", id));
    }

    @GetMapping("/usr/article/list")
    public String list(Model model,
            int boardId,
            @RequestParam(defaultValue = "1") int cPage,
            @RequestParam(defaultValue = "") String searchKeyword,
            String searchType) {

        BoardType boardType = requireBoardType(boardId);

        Member member = resolveCurrentMember();
        boolean isAdmin = member != null && member.getAuthLevel() == 0;
        boolean isLoggedIn = member != null;

        int itemsInAPage = 10;
        int limitFrom = (cPage - 1) * itemsInAPage;

        int articlesCnt = this.articleService.getArticlesCnt(boardType.getId(), searchType, searchKeyword.trim());
        int totalPagesCnt = (int) Math.ceil(articlesCnt / (double) itemsInAPage);

        int begin = ((cPage - 1) / 10) * 10 + 1;
        int end = (((cPage - 1) / 10) + 1) * 10;
        if (end > totalPagesCnt) {
            end = totalPagesCnt;
        }

        List<Article> articles = this.articleService.showList(boardType.getId(), limitFrom, itemsInAPage, searchType,
                searchKeyword.trim());

        model.addAttribute("articles", articles);
        model.addAttribute("boardName", boardType.getDisplayName());
        model.addAttribute("totalPagesCnt", totalPagesCnt);
        model.addAttribute("articlesCnt", articlesCnt);
        model.addAttribute("begin", begin);
        model.addAttribute("end", end);
        model.addAttribute("cPage", cPage);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "usr/article/list";
    }

    @GetMapping("/usr/article/detail")
    public String detail(HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            int id) {

        Cookie[] cookies = request.getCookies();
        boolean isViewed = false;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("viewedArticle_" + id)) {
                    isViewed = true;
                    break;
                }
            }
        }

        if (!isViewed) {
            this.articleService.increaseViews(id);
            Cookie cookie = new Cookie("viewedArticle_" + id, "true");
            cookie.setMaxAge(60 * 30);
            response.addCookie(cookie);
        }

        Article article = this.articleService.getArticleById(id);
        if (article == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        requireBoardType(article.getBoardId());
        Member member = resolveCurrentMember();
        boolean canEdit = member != null && (isAdmin() || Objects.equals(member.getId(), article.getMemberId()));

        model.addAttribute("article", article);
        model.addAttribute("canEdit", canEdit);
        Integer loginedMemberId = resolveCurrentMemberId();
        int articleLikeCount = likePointService.getLikePointCnt("article", article.getId());
        boolean articleLiked = loginedMemberId != null
                && likePointService.getLikePoint(loginedMemberId, "article", article.getId()) != null;
        List<Reply> replies = replyService.getReplies("article", article.getId());
        if (replies == null) {
            replies = Collections.emptyList();
        }
        List<ReplyView> replyViews = replies.stream()
                .map(reply -> new ReplyView(
                        reply,
                        likePointService.getLikePointCnt("reply", reply.getId()),
                        loginedMemberId != null
                                && likePointService.getLikePoint(loginedMemberId, "reply", reply.getId()) != null,
                        loginedMemberId != null && loginedMemberId.equals(reply.getMemberId())
                ))
                .toList();

        model.addAttribute("articleLikeCount", articleLikeCount);
        model.addAttribute("articleLiked", articleLiked);
        model.addAttribute("replyViews", replyViews);
        model.addAttribute("loginedMemberId", loginedMemberId);

        return "usr/article/detail";
    }

    @GetMapping("/usr/article/modify")
    public String modify(Model model, int id) {

        Article article = this.articleService.getArticleById(id);
        if (article == null) {
            return Util.jsReplace("존재하지 않는 글입니다.", "/usr/article/list?boardId=1");
        }
        BoardType boardType = requireBoardType(article.getBoardId());
        if (isNoticeBoard(boardType) && !isAdmin()) {
            req.jsPrintReplace("공지사항은 관리자만 수정할 수 있습니다.", "/usr/article/detail?id=" + id);
            return null;
        }
        model.addAttribute("article", article);

        return "usr/article/modify";
    }

    @PostMapping("/usr/article/doModify")
    @ResponseBody
    public String doModify(int id, String title, String content) {
        Article article = this.articleService.getArticleById(id);
        if (article == null) {
            return Util.jsReplace("존재하지 않는 글입니다.", "/usr/article/list");
        }
        BoardType boardType = requireBoardType(article.getBoardId());
        if (isNoticeBoard(boardType) && !isAdmin()) {
            return Util.jsReplace("공지사항은 관리자만 수정할 수 있습니다.", String.format("detail?id=%d", id));
        }

        Member member = resolveCurrentMember();
        if (member == null) {
            return Util.jsReplace("로그인 후 이용해주세요.", "/login");
        }
        if (!isAdmin() && !Objects.equals(member.getId(), article.getMemberId())) {
            return Util.jsReplace("작성자만 수정할 수 있습니다.", String.format("detail?id=%d", id));
        }

        this.articleService.modifyArticle(id, title, content);
        return Util.jsReplace("게시글이 수정되었습니다.", String.format("detail?id=%d", id));
    }

    @GetMapping("/usr/article/delete")
    @ResponseBody
    public String delete(int id, int boardId) {

        Article article = this.articleService.getArticleById(id);
        if (article == null) {
            return Util.jsReplace("존재하지 않는 글입니다.", "/usr/article/list?boardId=" + boardId);
        }
        BoardType boardType = requireBoardType(article.getBoardId());
        if (isNoticeBoard(boardType) && !isAdmin()) {
            return Util.jsReplace("공지사항은 관리자만 삭제할 수 있습니다.", "/usr/article/detail?id=" + id);
        }

        this.articleService.deleteArticle(id);

        return Util.jsReplace(
                String.format("%d번 글이 삭제되었습니다.", id),
                String.format("list?boardId=%d", boardId));
    }

    private Integer resolveCurrentMemberId() {
        Member member = resolveCurrentMember();
        return member != null ? member.getId() : null;
    }

    public static class ReplyView {
        private final Reply reply;
        private final int likeCount;
        private final boolean liked;
        private final boolean mine;

        public ReplyView(Reply reply, int likeCount, boolean liked, boolean mine) {
            this.reply = reply;
            this.likeCount = likeCount;
            this.liked = liked;
            this.mine = mine;
        }

        public Reply getReply() {
            return reply;
        }

        public int getLikeCount() {
            return likeCount;
        }

        public boolean isLiked() {
            return liked;
        }

        public boolean isMine() {
            return mine;
        }
    }
}
