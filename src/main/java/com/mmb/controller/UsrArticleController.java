// src/main/java/com/mmb/controller/UsrArticleController.java
package com.mmb.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmb.dto.Article;
import com.mmb.dto.Req;
import com.mmb.entity.Member;
import com.mmb.service.ArticleService;
import com.mmb.service.BoardService;
import com.mmb.service.MemberService;
import com.mmb.util.Util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UsrArticleController {

    private final ArticleService articleService;
    private final BoardService boardService;
    private final Req req;
    private final MemberService memberService;
    private static final int NOTICE_BOARD_ID = 1;

    public UsrArticleController(ArticleService articleService, BoardService boardService, Req req, MemberService memberService) {
        this.articleService = articleService;
        this.boardService = boardService;
        this.req = req;
        this.memberService = memberService;
    }

    private boolean isNoticeBoard(int boardId) {
        return boardId == NOTICE_BOARD_ID;
    }

    private boolean isAdmin() {
        Member member = resolveCurrentMember();
        return member != null && member.getAuthLevel() != null && member.getAuthLevel() == 0;
    }

    private Member resolveCurrentMember() {
        if (req.getLoginedMember() != null && req.getLoginedMember().getId() > 0) {
            return memberService.findById((long) req.getLoginedMember().getId()).orElse(null);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return memberService.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping("/usr/article/write")
    public String write(Model model, int boardId) {
        if (isNoticeBoard(boardId) && !isAdmin()) {
            req.jsPrintReplace("공지사항은 관리자만 작성할 수 있습니다.", "/usr/article/list?boardId=" + boardId);
            return null;
        }
        model.addAttribute("boardId", boardId);
        return "usr/article/write";
    }

    @PostMapping("/usr/article/doWrite")
    @ResponseBody
    public String doWrite(String title, String content, int boardId) {
        if (isNoticeBoard(boardId) && !isAdmin()) {
            return Util.jsReplace("공지사항은 관리자만 작성할 수 있습니다.", "/usr/article/list?boardId=" + boardId);
        }

        Member member = resolveCurrentMember();
        if (member == null) {
            return Util.jsReplace("로그인 후 이용해주세요.", "/login");
        }

        this.articleService.writeArticle(title, content, member.getId().intValue(), boardId);
        int id = this.articleService.getLastInsertId();

        return Util.jsReplace("게시글이 작성되었습니다.", String.format("detail?id=%d", id));
    }

    @GetMapping("/usr/article/list")
    public String list(Model model,
            int boardId,
            @RequestParam(defaultValue = "1") int cPage,
            @RequestParam(defaultValue = "") String searchKeyword,
            String searchType) {

        Member member = resolveCurrentMember();
        boolean isAdmin = member != null && member.getAuthLevel() != null && member.getAuthLevel() == 0;
        boolean isLoggedIn = member != null;

        int itemsInAPage = 10;
        int limitFrom = (cPage - 1) * itemsInAPage;

        int articlesCnt = this.articleService.getArticlesCnt(boardId, searchType, searchKeyword.trim());
        int totalPagesCnt = (int) Math.ceil(articlesCnt / (double) itemsInAPage);

        int begin = ((cPage - 1) / 10) * 10 + 1;
        int end = (((cPage - 1) / 10) + 1) * 10;
        if (end > totalPagesCnt) {
            end = totalPagesCnt;
        }

        List<Article> articles = this.articleService.showList(boardId, limitFrom, itemsInAPage, searchType,
                searchKeyword.trim());
        String boardName = this.boardService.getBoardNameById(boardId);

        model.addAttribute("articles", articles);
        model.addAttribute("boardName", boardName);
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
        Member member = resolveCurrentMember();
        boolean canEdit = member != null && (isAdmin() || member.getId().equals((long) article.getMemberId()));

        model.addAttribute("article", article);
        model.addAttribute("canEdit", canEdit);

        return "usr/article/detail";
    }

    @GetMapping("/usr/article/modify")
    public String modify(Model model, int id) {

        Article article = this.articleService.getArticleById(id);
        if (article != null && isNoticeBoard(article.getBoardId()) && !isAdmin()) {
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
        if (isNoticeBoard(article.getBoardId()) && !isAdmin()) {
            return Util.jsReplace("공지사항은 관리자만 수정할 수 있습니다.", String.format("detail?id=%d", id));
        }

        Member member = resolveCurrentMember();
        if (member == null) {
            return Util.jsReplace("로그인 후 이용해주세요.", "/login");
        }
        if (!isAdmin() && !member.getId().equals((long) article.getMemberId())) {
            return Util.jsReplace("작성자만 수정할 수 있습니다.", String.format("detail?id=%d", id));
        }

        this.articleService.modifyArticle(id, title, content);
        return Util.jsReplace("게시글을 수정했습니다.", String.format("detail?id=%d", id));
    }

    @GetMapping("/usr/article/delete")
    @ResponseBody
    public String delete(int id, int boardId) {

        Article article = this.articleService.getArticleById(id);
        if (article != null && isNoticeBoard(article.getBoardId()) && !isAdmin()) {
            return Util.jsReplace("공지사항은 관리자만 삭제할 수 있습니다.", "/usr/article/detail?id=" + id);
        }

        this.articleService.deleteArticle(id);

        return Util.jsReplace(
                String.format("%d번 글을 삭제했습니다.", id),
                String.format("list?boardId=%d", boardId));
    }
}
