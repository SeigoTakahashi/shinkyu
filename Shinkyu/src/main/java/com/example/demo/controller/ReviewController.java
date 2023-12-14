package com.example.demo.controller;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class ReviewController {

	@Autowired
	JdbcTemplate jdbcTemplate;

	private HttpSession session;

	@Autowired
	public ReviewController(HttpSession session) {
		this.session = session;
	}

	@RequestMapping(path = "/index", method = RequestMethod.GET)
	public String home(Model model) throws IOException {
		String userId = (String) session.getAttribute("id");
		if (userId == null) {
			List<Map<String, Object>> reviewList = jdbcTemplate.queryForList(
					"SELECT * FROM review INNER JOIN user ON review.UserID = user.UserID WHERE Open = true;");
			model.addAttribute("reviewList", reviewList);
			List<Map<String, Object>> categoryList = jdbcTemplate
					.queryForList("SELECT DISTINCT(Category) FROM review WHERE Open = true;");
			model.addAttribute("categoryList", categoryList);
			model.addAttribute("lflg", 0);
			return "index";
		} else {
			List<Map<String, Object>> reviewList = jdbcTemplate.queryForList(
					"SELECT * FROM review INNER JOIN user ON review.UserID = user.UserID WHERE Open = true AND Category IN (SELECT Category FROM favorite WHERE UserID = ?);",
					userId);
			model.addAttribute("reviewList", reviewList);
			List<Map<String, Object>> categoryList = jdbcTemplate.queryForList(
					"SELECT DISTINCT(Category) FROM review WHERE Open = true AND Category IN (SELECT Category FROM favorite WHERE UserID = ?);",
					userId);
			model.addAttribute("categoryList", categoryList);
			List<Map<String, Object>> goodList = jdbcTemplate.queryForList(
					"SELECT ReviewID FROM good WHERE UserID = ?;",
					userId);
			model.addAttribute("goodList", goodList.stream().map(m -> m.get("ReviewID")).toList());
			model.addAttribute("lflg", 1);
			List<Map<String, Object>> followList = jdbcTemplate.queryForList(
					"SELECT FollowUserID FROM follow WHERE UserID = ?;",
					userId);
			model.addAttribute("followList", followList.stream().map(m -> m.get("FollowUserID")).toList());
			List<Map<String, Object>> commentList = jdbcTemplate
					.queryForList("SELECT * FROM reply INNER JOIN user ON reply.UserID = user.UserID;");
			model.addAttribute("commentList", commentList);
			return "index";
		}

	}

	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public String login(Model model) throws IOException {
		List<Map<String, Object>> categoryList = jdbcTemplate
				.queryForList("SELECT DISTINCT(Category) FROM review WHERE Open = true;");
		model.addAttribute("categoryList", categoryList);
		return "login";
	}

	@RequestMapping(path = "/login", method = RequestMethod.POST)
	public String check(RedirectAttributes redirectattribute, String userId, String password) throws IOException {
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE UserID = ? AND Password = ?", Integer.class, userId,password);
		if (count == 0) {
			redirectattribute.addFlashAttribute("error", 1);
			return "redirect:/login";
		} else {
			this.session.setAttribute("id", userId);
			redirectattribute.addFlashAttribute("login", 1);
			return "redirect:/mypage";
		}

	}

	@RequestMapping(path = "/logout", method = RequestMethod.GET)
	public String logout(RedirectAttributes redirectattribute) throws IOException {
		this.session.removeAttribute("id");
		List<Map<String, Object>> categoryList = jdbcTemplate
				.queryForList("SELECT DISTINCT(Category) FROM review WHERE Open = true;");
		redirectattribute.addFlashAttribute("categoryList", categoryList);
		return "redirect:/login";
	}

	@RequestMapping(path = "/signup", method = RequestMethod.GET)
	public String signupG(Model model) throws IOException {
		List<Map<String, Object>> categoryList = jdbcTemplate
				.queryForList("SELECT DISTINCT(Category) FROM review WHERE Open = true;");
		model.addAttribute("categoryList", categoryList);
		return "login";
	}

	@RequestMapping(path = "/signup", method = RequestMethod.POST)
	public String signupP(RedirectAttributes redirectAttributes, String userId, String password, String[] category,
			String gender, String birthday, MultipartFile icon) throws IOException {
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user WHERE UserID = ?", Integer.class, userId);
		if (count == 0) {
			byte[] byteData = icon.getBytes();
			String encodedImage = Base64.getEncoder().encodeToString(byteData);
			encodedImage = "data:image/png;base64," + encodedImage;
			int setGender = 0;
			if ("male".equals(gender)) {
				setGender = 1;
			} else if ("female".equals(gender)) {
				setGender = 0;
			}
			Date setBirthday = Date.valueOf(birthday);

			jdbcTemplate.update("INSERT INTO user VALUES(?,?,?,?,?)", userId, password, setGender, setBirthday,
					encodedImage);
			if (category != null) {
				for (String c : category) {
					jdbcTemplate.update("INSERT INTO favorite VALUES(?,?);", userId, c);
				}
			}

			this.session.setAttribute("id", userId);
			redirectAttributes.addFlashAttribute("login", 1);
			return "redirect:/mypage";
		} else {
			redirectAttributes.addFlashAttribute("error", 2);
			return "redirect:/login";
		}
	}

	@RequestMapping(path = "/mypage", method = RequestMethod.GET)
	public String mypage(Model model) throws IOException {
		String userId = (String) session.getAttribute("id");

		if (userId == null) {
			return "redirect:/login";
		} else {
			List<Map<String, Object>> myreview = jdbcTemplate.queryForList("SELECT * FROM review WHERE UserID = ?;",
					userId);
			model.addAttribute("myreview", myreview);
			List<Map<String, Object>> good = jdbcTemplate.queryForList("SELECT * FROM good WHERE UserID = ?;",
					userId);
			model.addAttribute("good", good);
			List<Map<String, Object>> categoryList = jdbcTemplate.queryForList(
					"SELECT DISTINCT(Category) FROM review WHERE UserID = ?;",
					userId);
			model.addAttribute("categoryList", categoryList);
			List<Map<String, Object>> goodList = jdbcTemplate.queryForList(
					"SELECT ReviewID FROM good WHERE UserID = ?;",
					userId);
			model.addAttribute("goodList", goodList.stream().map(m -> m.get("ReviewID")).toList());
			List<Map<String, Object>> commentList = jdbcTemplate
					.queryForList("SELECT * FROM reply INNER JOIN user ON reply.UserID = user.UserID;");
			model.addAttribute("commentList", commentList);
			return "mypage";
		}

	}

	@RequestMapping(path = "/review", method = RequestMethod.GET)
	public String review(Model model) throws IOException {
		String userId = (String) session.getAttribute("id");

		if (userId == null) {
			return "redirect:/login";
		} else {
			List<Map<String, Object>> categoryList = jdbcTemplate
					.queryForList("SELECT DISTINCT(Category) FROM review WHERE UserID = ?;", userId);
			model.addAttribute("categoryList", categoryList);
			return "review";
		}

	}

	@RequestMapping(path = "/review", method = RequestMethod.POST)
	public String reviewPost(String title, String category, String comment, String reviewNum, MultipartFile image,
			String open) throws IOException {
		String userId = (String) session.getAttribute("id");

		byte[] byteData = image.getBytes();
		String encodedImage = Base64.getEncoder().encodeToString(byteData);
		encodedImage = "data:image/png;base64," + encodedImage;

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		jdbcTemplate.update("INSERT INTO review VALUES(?,?,?,?,?,?,?,?,?,?,?);", null, userId, encodedImage, comment,
				title, Integer.parseInt(reviewNum), Integer.parseInt(open), 0, category, timestamp, 0);

		return "redirect:/mypage";

	}

	@RequestMapping(path = "/account", method = RequestMethod.GET)
	public String account(Model model) throws IOException {
		String userId = (String) session.getAttribute("id");

		if (userId == null) {
			return "redirect:/login";
		} else {
			List<Map<String, Object>> userInfo = jdbcTemplate.queryForList("SELECT * FROM user WHERE UserID = ?;",
					userId);
			List<Map<String, Object>> categoryList = jdbcTemplate
					.queryForList("SELECT DISTINCT(Category) FROM favorite WHERE UserID = ?;", userId);
			model.addAttribute("categoryList", categoryList);
			model.addAttribute("userInfo", userInfo);
			int follow = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM follow WHERE UserID = ?", Integer.class,
					userId);
			int follower = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM follow WHERE FollowUserID = ?",
					Integer.class, userId);
			model.addAttribute("follow", follow);
			model.addAttribute("follower", follower);
			return "account";
		}

	}

	@RequestMapping(path = "/goodMyPage/{reviewId}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void good(RedirectAttributes redirectAttributes, @PathVariable("reviewId") String reviewId)
			throws IOException {
		String userId = (String) session.getAttribute("id");
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM good WHERE UserID = ? AND ReviewID = ?",
				Integer.class, userId, reviewId);
		if (count == 0) {
			jdbcTemplate.update("INSERT INTO good VALUES(?,?,?)", null, userId, reviewId);
			jdbcTemplate.update("UPDATE review SET Good = Good + 1 WHERE ReviewID = ?", reviewId);
		} else {
			jdbcTemplate.update("DELETE FROM good WHERE UserID = ? AND ReviewID = ?", userId, reviewId);
			jdbcTemplate.update("SET @i := 0");
			jdbcTemplate.update("UPDATE good SET GoodID = (@i := @i + 1)");
			jdbcTemplate.execute("ALTER TABLE good auto_increment = 1");
			jdbcTemplate.update("UPDATE review SET Good = Good - 1 WHERE ReviewID = ?", reviewId);
		}
	}

	@RequestMapping(path = "/likes", method = RequestMethod.GET)
	public String likes(Model model) throws IOException {
		String userId = (String) session.getAttribute("id");

		if (userId == null) {
			return "redirect:/login";
		} else {
			List<Map<String, Object>> myreview = jdbcTemplate.queryForList(
					"SELECT * FROM review INNER JOIN user ON review.UserID = user.UserID INNER JOIN good ON review.ReviewID = good.ReviewID WHERE good.UserID = ? AND review.Open = true;",
					userId);
			model.addAttribute("myreview", myreview);
			List<Map<String, Object>> categoryList = jdbcTemplate.queryForList(
					"SELECT DISTINCT(Category) FROM review INNER JOIN user ON review.UserID = user.UserID INNER JOIN good ON review.ReviewID = good.ReviewID WHERE review.Open = true;");
			model.addAttribute("categoryList", categoryList);
			List<Map<String, Object>> goodList = jdbcTemplate.queryForList(
					"SELECT ReviewID FROM good WHERE UserID = ?;",
					userId);
			model.addAttribute("goodList", goodList.stream().map(m -> m.get("ReviewID")).toList());
			List<Map<String, Object>> followList = jdbcTemplate.queryForList(
					"SELECT FollowUserID FROM follow WHERE UserID = ?;",
					userId);
			model.addAttribute("followList", followList.stream().map(m -> m.get("FollowUserID")).toList());
			List<Map<String, Object>> commentList = jdbcTemplate
					.queryForList("SELECT * FROM reply INNER JOIN user ON reply.UserID = user.UserID;");
			model.addAttribute("commentList", commentList);
			return "likes";
		}

	}

	@RequestMapping(path = "/delete/{reviewId}", method = RequestMethod.GET)
	public String good(@PathVariable("reviewId") String reviewId)
			throws IOException {
		jdbcTemplate.update("DELETE FROM review WHERE ReviewID = ?", reviewId);
		return "redirect:/mypage";
	}

	@RequestMapping(path = "/follow/{followUserId}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void follow(@PathVariable("followUserId") String followUserId) throws IOException {
		String userId = (String) session.getAttribute("id");
		int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM follow WHERE UserID = ? AND FollowUserID = ?",
				Integer.class, userId, followUserId);
		if (count == 0) {
			jdbcTemplate.update("INSERT INTO follow VALUES(?,?,?)", null, userId, followUserId);
		} else {
			jdbcTemplate.update("DELETE FROM follow WHERE UserID = ? AND FollowUserID = ?", userId, followUserId);
			jdbcTemplate.update("SET @i := 0");
			jdbcTemplate.update("UPDATE follow SET FollowID = (@i := @i + 1)");
			jdbcTemplate.execute("ALTER TABLE follow auto_increment = 1");

		}
	}

	@RequestMapping(path = "/editGender/{afterGender}", method = RequestMethod.GET)
	public String editGender(@PathVariable("afterGender") String afterGender) throws IOException {
		String userId = (String) this.session.getAttribute("id");
		int value = 0;
		if ("男性".equals(afterGender)) {
			value = 1;
		} else if ("女性".equals(afterGender)) {
			value = 0;
		}
		jdbcTemplate.update("UPDATE user SET Gender = ? WHERE UserID = ?;", value, userId);
		return "redirect:/account";
	}

	@RequestMapping(path = "/editBirthday/{afterBirthday}", method = RequestMethod.GET)
	public String editBirthday(@PathVariable("afterBirthday") String afterBirthday) throws IOException {
		String userId = (String) this.session.getAttribute("id");
		jdbcTemplate.update("UPDATE user SET Birthday = ? WHERE UserID = ?;", afterBirthday, userId);
		return "redirect:/account";
	}

	@RequestMapping(path = "/editCategory/{afterCategory}", method = RequestMethod.GET)
	public String editCategory(@PathVariable("afterCategory") String afterCategory) throws IOException {
		String userId = (String) this.session.getAttribute("id");
		jdbcTemplate.update("INSERT INTO favorite VALUES(?,?)", userId, afterCategory);
		return "redirect:/account";
	}

	@RequestMapping(path = "/editOpen/{reviewId}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void editOpen(@PathVariable String reviewId) throws IOException {
		String userId = (String) this.session.getAttribute("id");
		jdbcTemplate.update(
				"UPDATE review SET Open = CASE WHEN Open = 0 THEN 1 WHEN Open = 1 THEN Open = 0 END WHERE ReviewID = ?",
				reviewId);
	}

	@RequestMapping(path = "/follows", method = RequestMethod.GET)
	public String follows(Model model) throws IOException {
		String userId = (String) session.getAttribute("id");
		if (userId == null) {
			return "redirect:/login";
		} else {
			List<Map<String, Object>> myreview = jdbcTemplate.queryForList(
					"SELECT * FROM review INNER JOIN user ON review.UserID = user.UserID INNER JOIN follow ON review.UserID = follow.FollowUserID WHERE follow.UserID = ? AND review.Open = true;",
					userId);
			model.addAttribute("myreview", myreview);
			List<Map<String, Object>> goodList = jdbcTemplate.queryForList(
					"SELECT ReviewID FROM good WHERE UserID = ?;",
					userId);
			model.addAttribute("goodList", goodList.stream().map(m -> m.get("ReviewID")).toList());
			List<Map<String, Object>> followList = jdbcTemplate.queryForList(
					"SELECT FollowUserID FROM follow WHERE UserID = ?;",
					userId);
			model.addAttribute("followList", followList.stream().map(m -> m.get("FollowUserID")).toList());
			List<Map<String, Object>> commentList = jdbcTemplate
					.queryForList("SELECT * FROM reply INNER JOIN user ON reply.UserID = user.UserID;");
			model.addAttribute("commentList", commentList);
			return "follows";
		}

	}

	@RequestMapping(path = "/commentIndex", method = RequestMethod.POST)
	public String commentI(String reply, String userId, String reviewId) throws IOException {
		jdbcTemplate.update("INSERT INTO reply VALUES(?,?,?,?);", null, reviewId, userId, reply);
		jdbcTemplate.update("UPDATE review SET ReplyNum = ReplyNum + 1 WHERE ReviewID = ?", reviewId);
		return "redirect:/index";
	}

	@RequestMapping(path = "/commentMypage", method = RequestMethod.POST)
	public String commentM(String reply, String userId, String reviewId) throws IOException {
		jdbcTemplate.update("INSERT INTO reply VALUES(?,?,?,?);", null, reviewId, userId, reply);
		jdbcTemplate.update("UPDATE review SET ReplyNum = ReplyNum + 1 WHERE ReviewID = ?", reviewId);
		return "redirect:/mypage";
	}

	@RequestMapping(path = "/commentLikes", method = RequestMethod.POST)
	public String commentL(String reply, String userId, String reviewId) throws IOException {
		jdbcTemplate.update("INSERT INTO reply VALUES(?,?,?,?);", null, reviewId, userId, reply);
		jdbcTemplate.update("UPDATE review SET ReplyNum = ReplyNum + 1 WHERE ReviewID = ?", reviewId);
		return "redirect:/likes";
	}

	@RequestMapping(path = "/commentFollows", method = RequestMethod.POST)
	public String commentF(String reply, String userId, String reviewId) throws IOException {
		jdbcTemplate.update("INSERT INTO reply VALUES(?,?,?,?);", null, reviewId, userId, reply);
		jdbcTemplate.update("UPDATE review SET ReplyNum = ReplyNum + 1 WHERE ReviewID = ?", reviewId);
		return "redirect:/follows";
	}


	@RequestMapping(path = "/update/{reviewId}", method = RequestMethod.GET)
	public String update(@PathVariable String reviewId,Model model) throws IOException {
		String userId = (String) session.getAttribute("id");
		List<Map<String, Object>> targetReview = jdbcTemplate.queryForList("SELECT * FROM review WHERE ReviewID = ?",
				reviewId);
		model.addAttribute("targetReview", targetReview);
		List<Map<String, Object>> categoryList = jdbcTemplate
				.queryForList("SELECT DISTINCT(Category) FROM review WHERE UserID = ?;", userId);
		model.addAttribute("categoryList", categoryList);
		return "update";
	}

	@RequestMapping(path = "/update", method = RequestMethod.POST)
	public String updatePost(String title, String category, String comment, String reviewNum, MultipartFile image,
			String open, String reviewId) throws IOException {
		String userId = (String) session.getAttribute("id");

		byte[] byteData = image.getBytes();
		String encodedImage = Base64.getEncoder().encodeToString(byteData);
		encodedImage = "data:image/png;base64," + encodedImage;

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		jdbcTemplate.update(
				"UPDATE review SET UserID = ?,Image = ?,Comment = ?,Title = ?,Reviews = ?,Open = ?,Category = ?,Update_date = ? WHERE ReviewID = ?;",
				userId, encodedImage, comment,
				title, Integer.parseInt(reviewNum), Integer.parseInt(open), category, timestamp, reviewId);
		session.removeAttribute("targetReview");
		return "redirect:/mypage";
	}

	@RequestMapping(path = "/deleteComment/{replyId}/{reviewId}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteComment(@PathVariable String replyId, @PathVariable String reviewId) throws IOException {
		jdbcTemplate.update("DELETE FROM reply WHERE ReplyID = ?", replyId);
		jdbcTemplate.update("UPDATE review SET ReplyNum = ReplyNum - 1 WHERE ReviewID = ?", reviewId);
	}
	
	@RequestMapping(path = "/other/{otherUserId}", method = RequestMethod.GET)
	public String other(@PathVariable String otherUserId,Model model) throws IOException {
		String userId = (String) session.getAttribute("id");
		List<Map<String, Object>> userInfo = jdbcTemplate.queryForList("SELECT * FROM user WHERE UserID = ?;",
				otherUserId);
		List<Map<String, Object>> categoryList = jdbcTemplate
				.queryForList("SELECT DISTINCT(Category) FROM favorite WHERE UserID = ?;", otherUserId);
		model.addAttribute("categoryList", categoryList);
		model.addAttribute("userInfo", userInfo);
		int follow = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM follow WHERE UserID = ?", Integer.class,
				otherUserId);
		int follower = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM follow WHERE FollowUserID = ?",
				Integer.class, otherUserId);
		model.addAttribute("follow", follow);
		model.addAttribute("follower", follower);
		List<Map<String, Object>> followList = jdbcTemplate.queryForList(
				"SELECT FollowUserID FROM follow WHERE UserID = ?;",
				userId);
		model.addAttribute("followList", followList.stream().map(m -> m.get("FollowUserID")).toList());
		return "other";
	}

}