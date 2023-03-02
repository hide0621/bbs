package com.example.app.bbs.app.controller

import com.example.app.bbs.app.request.ArticleRequest
import com.example.app.bbs.domain.entity.Article
import com.example.app.bbs.domain.repository.ArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.*

@Controller
class ArticleController {

    @Autowired
    lateinit var articleRepository: ArticleRepository

    val MESSAGE_REGISTER_NORMAL = "正常に投稿できました。"

    val MESSAGE_ARTICLE_DOES_NOT_EXISTS = "対象の記事が見つかりませんでした。"

    val MESSAGE_UPDATE_NORMAL = "正常に更新しました。"

    val MESSAGE_ARTICLE_KEY_UNMATCH = "投稿 KEY が一致しません。"

    val ALERT_CLASS_ERROR = "alert-error"

    @PostMapping("/")
    fun registerArticle(@ModelAttribute articleRequest: ArticleRequest, redirectAttributes: RedirectAttributes): String {
        articleRepository.save(
            Article(
                articleRequest.id,
                articleRequest.name,
                articleRequest.title,
                articleRequest.contents,
                articleRequest.articleKey
            )
        )

        redirectAttributes.addFlashAttribute("message", MESSAGE_REGISTER_NORMAL)

        return "redirect:/"
    }

    @GetMapping("/")
    fun getArticleList(model : Model) : String {
        model.addAttribute("articles", articleRepository.findAll())
        return "index"
    }

    @GetMapping("/edit/{id}")
    fun getArticleEdit(@PathVariable id: Int, model: Model, redirectAttributes: RedirectAttributes): String {

        return if (articleRepository.existsById(id)) {
            model.addAttribute("article", articleRepository.findById(id))
            "edit"
        } else {
            redirectAttributes.addFlashAttribute("message",MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class",ALERT_CLASS_ERROR)
            "redirect:/"
        }
    }

    @PostMapping("/update")
    fun updateArticle(articleRequest: ArticleRequest, redirectAttributes: RedirectAttributes): String {
        if (!articleRepository.existsById(articleRequest.id)) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_DOES_NOT_EXISTS)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/"
        }

        val article: Article = articleRepository.findById(articleRequest.id).get()

        if (articleRequest.articleKey != article.articleKey) {
            redirectAttributes.addFlashAttribute("message", MESSAGE_ARTICLE_KEY_UNMATCH)
            redirectAttributes.addFlashAttribute("alert_class", ALERT_CLASS_ERROR)
            return "redirect:/edit/${articleRequest.id}"
        }

        article.name = articleRequest.name
        article.title = articleRequest.title
        article.contents = articleRequest.contents
        article.updateAt = Date()

        articleRepository.save(article)

        redirectAttributes.addFlashAttribute("message", MESSAGE_UPDATE_NORMAL)

        return "redirect:/"

    }

    @GetMapping("/delete/confirm/{id}")
    fun getDeleteConfirm(@PathVariable id: Int, model: Model): String {

        if (!articleRepository.existsById(id)) {
            return "redirect:/"
        }

        model.addAttribute("article", articleRepository.findById(id).get())

        return "delete_confirm"
    }

    @PostMapping("/delete")
    fun deleteArticle(@ModelAttribute articleRequest: ArticleRequest): String {

        if (!articleRepository.existsById(articleRequest.id)) {
            return "redirect:/"
        }

        val article: Article = articleRepository.findById(articleRequest.id).get()

        if (articleRequest.articleKey != article.articleKey) {
            return "redirect:/delete/confirm/${article.id}"
        }

        articleRepository.deleteById(articleRequest.id)

        return "redirect:/"
    }

}