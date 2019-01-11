package cc.ryanc.halo.web.controller.api;

import cc.ryanc.halo.model.domain.Comment;
import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.JsonResult;
import cc.ryanc.halo.model.dto.ListPage;
import cc.ryanc.halo.model.enums.BlogPropertiesEnum;
import cc.ryanc.halo.model.enums.PostStatusEnum;
import cc.ryanc.halo.model.enums.PostTypeEnum;
import cc.ryanc.halo.model.enums.ResponseStatusEnum;
import cc.ryanc.halo.model.request.LikeR;
import cc.ryanc.halo.model.request.UserR;
import cc.ryanc.halo.service.PostService;
import cc.ryanc.halo.utils.CommentUtil;
import cn.hutool.core.util.StrUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * <pre>
 *     文章API
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/6/6
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/posts")
public class ApiPostController {

    @Autowired
    private PostService postService;

    /**
     * 获取文章列表 分页
     * @param page 页码
     * @return JsonResult
     */
    @GetMapping(value = "/page/{page}")
    public JsonResult posts(@PathVariable(value = "page") Integer page) {
        final Sort sort = new Sort(Sort.Direction.DESC, "postDate");
        int size = 10;
        if (StrUtil.isNotBlank(HaloConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()))) {
            size = Integer.parseInt(HaloConst.OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()));
        }
        final Pageable pageable = PageRequest.of(page - 1, size, sort);
        final Page<Post> posts = postService.findPostByStatus(PostStatusEnum.PUBLISHED.getCode(), PostTypeEnum.POST_TYPE_POST.getDesc(), pageable);
        if (null == posts) {
            return new JsonResult(ResponseStatusEnum.EMPTY.getCode(), ResponseStatusEnum.EMPTY.getMsg());
        }
        return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), posts);
    }

    /**
     * 获取单个文章信息
     * @param postId 文章编号
     * @return JsonResult
     */
    @GetMapping(value = "/{postId}")
    public JsonResult posts(@PathVariable(value = "postId") Long postId) {
        final Post post = postService.findByPostId(postId, PostTypeEnum.POST_TYPE_POST.getDesc());
        final ListPage<Comment> commentsPage = new ListPage<>(CommentUtil.getComments(post.getComments()), 1, 10);
        post.setComments(CommentUtil.only2Top(commentsPage.getData()));
        postService.cacheViews(post.getPostId());
        return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), post);
    }

    /**
     * 获取轮播图文章
     * @return JsonResult
     */
    @GetMapping(value = "/swiper")
    public JsonResult swiperPosts() {
        final List<Post> post = postService.getSwiperPosts();
        if (null != post) {
            return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg(), post);
        } else {
            return new JsonResult(ResponseStatusEnum.NOTFOUND.getCode(), ResponseStatusEnum.NOTFOUND.getMsg());
        }
    }

    @PostMapping(value = "/like")
    public JsonResult likePost(@RequestBody LikeR likeR) {
        postService.likePost(likeR);
        return new JsonResult(ResponseStatusEnum.SUCCESS.getCode(), ResponseStatusEnum.SUCCESS.getMsg());
    }

}
