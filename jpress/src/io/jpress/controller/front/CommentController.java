package io.jpress.controller.front;

import java.math.BigInteger;
import java.util.Date;

import io.jpress.Consts;
import io.jpress.core.JBaseController;
import io.jpress.core.annotation.UrlMapping;
import io.jpress.core.cache.ActionCacheManager;
import io.jpress.model.Comment;
import io.jpress.model.Content;
import io.jpress.model.Option;
import io.jpress.model.User;
import io.jpress.utils.CookieUtils;
import io.jpress.utils.StringUtils;

@UrlMapping(url = "/comment")
public class CommentController extends JBaseController {

	public void index() {

	}

	public void submit() {
		String gotoUrl = getPara("goto");
		if (gotoUrl == null) {
			gotoUrl = getRequest().getHeader("Referer");
		}
		
		String anchor = getPara("anchor");
		if(gotoUrl != null && anchor!=null ){
			gotoUrl += "#"+anchor;
		}
		
		BigInteger userId = StringUtils.toBigInteger(CookieUtils.get(this, Consts.COOKIE_LOGINED_USER), null);

		// 必须登录
		Boolean comment_must_logined = Option.findValueAsBool("comment_must_logined");
		if (comment_must_logined != null && comment_must_logined) {
			if (userId == null) {
				if (isAjaxRequest()) {
					renderAjaxResultForError("user not logined!");
				} else {
					renderText("comment fail,user not logined");
				}
				return;
			}
		}

		String status = Comment.STATUS_NORMAL;
		Boolean comment_must_audited = Option.findValueAsBool("comment_must_audited");
		if (comment_must_audited != null && comment_must_audited) {
			status = Comment.STATUS_DRAFT;
		}

		BigInteger contentId = getParaToBigInteger("cid");
		Content content = null;
		if (contentId != null) {
			content = Content.DAO.findById(contentId);
		} else {
			if (isAjaxRequest()) {
				renderAjaxResultForError("content id is null!");
			} else {
				renderText("comment fail,content id is null!");
			}
			return;
		}

		String author = getPara("author");
		String text = getPara("text");
		String email = getPara("email");

		String ip = getIPAddress();
		String agent = getUserAgent();
		String type = Comment.TYPE_COMMENT;

		if (userId != null) {
			User user = User.DAO.findById(userId);
			if (user != null && StringUtils.isNotBlank(user.getNickname())) {
				author = user.getNickname();
			} else {
				author = user.getUsername();
			}
		}

		Comment comment = new Comment();
		comment.setContentModule(content.getModule());
		comment.setContentId(content.getId());
		comment.setText(text);
		comment.setIp(ip);
		comment.setAgent(agent);
		comment.setAuthor(author);
		comment.setEmail(email);
		comment.setType(type);
		comment.setStatus(status);
		comment.setUserId(userId);
		comment.setCreated(new Date());
		comment.save();
		
		if (isAjaxRequest()) {
			renderAjaxResultForSuccess();
		} else {
			if (gotoUrl != null) {
				redirect(gotoUrl);
			}else{
				renderText("comment ok");
			}
		}
		
		ActionCacheManager.clearCache();
	}

	private String getIPAddress() {
		String ip = getRequest().getHeader("X-getRequest()ed-For");
		if (!StringUtils.isNotBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = getRequest().getHeader("X-Forwarded-For");
		}
		if (!StringUtils.isNotBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = getRequest().getHeader("Proxy-Client-IP");
		}
		if (!StringUtils.isNotBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = getRequest().getHeader("WL-Proxy-Client-IP");
		}
		if (!StringUtils.isNotBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = getRequest().getHeader("HTTP_CLIENT_IP");
		}
		if (!StringUtils.isNotBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = getRequest().getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (!StringUtils.isNotBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = getRequest().getRemoteAddr();
		}
		return ip;
	}

	private String getUserAgent() {
		return getRequest().getHeader("User-Agent");
	}

}
