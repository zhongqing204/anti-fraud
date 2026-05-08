import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/manager',
      component: () => import('../views/Manager.vue'),
      children: [
        { path: 'home', meta: { name: '系统首页' }, component: () => import('../views/admin/Home.vue') },
        { path: 'admin', meta: { name: '管理员信息' }, component: () => import('../views/admin/Admin.vue') },
        { path: 'notice', meta: { name: '系统公告' }, component: () => import('../views/admin/Notice.vue') },
        { path: 'carousel', meta: { name: '轮播图管理' }, component: () => import('../views/admin/Carousel.vue') },
        { path: 'category', meta: { name: '反诈分类' }, component: () => import('../views/admin/Category.vue') },
        { path: 'publicity', meta: { name: '反诈宣传' }, component: () => import('../views/admin/Publicity.vue') },
        { path: 'publicityLikes', meta: { name: '宣传点赞' }, component: () => import('../views/admin/PublicityLikes.vue') },
        { path: 'publicityCollects', meta: { name: '宣传收藏' }, component: () => import('../views/admin/PublicityCollects.vue') },
        { path: 'publicityComments', meta: { name: '宣传评论' }, component: () => import('../views/admin/PublicityComments.vue') },
        { path: 'video', meta: { name: '反诈视频' }, component: () => import('../views/admin/Video.vue') },
        { path: 'videoLikes', meta: { name: '视频点赞' }, component: () => import('../views/admin/VideoLikes.vue') },
        { path: 'videoCollects', meta: { name: '视频收藏' }, component: () => import('../views/admin/VideoCollects.vue') },
        { path: 'videoComments', meta: { name: '视频评论' }, component: () => import('../views/admin/VideoComments.vue') },
        { path: 'articleReport', meta: { name: '帖子举报管理' }, component: () => import('../views/admin/ArticleReport.vue') },
        { path: 'activity', meta: { name: '反诈活动' }, component: () => import('../views/admin/Activity.vue') },
        { path: 'activitySignUp', meta: { name: '报名管理' }, component: () => import('../views/admin/ActivitySignUp.vue') },
        { path: 'activityLikes', meta: { name: '活动点赞' }, component: () => import('../views/admin/ActivityLikes.vue') },
        { path: 'activityCollects', meta: { name: '活动收藏' }, component: () => import('../views/admin/ActivityCollects.vue') },
        { path: 'activityComments', meta: { name: '活动评论' }, component: () => import('../views/admin/ActivityComments.vue') },
        { path: 'article', meta: { name: '贴子管理' }, component: () => import('../views/admin/Article.vue') },
        { path: 'likes', meta: { name: '点赞信息' }, component: () => import('../views/admin/Likes.vue') },
        { path: 'collect', meta: { name: '收藏信息' }, component: () => import('../views/admin/Collect.vue') },
        { path: 'comment', meta: { name: '评论信息' }, component: () => import('../views/admin/Comment.vue') },
        { path: 'articleReport', meta: { name: '帖子举报管理' }, component: () => import('../views/admin/ArticleReport.vue') },
        { path: 'report', meta: { name: '举报管理' }, component: () => import('../views/admin/Report.vue') },
        { path: 'person', meta: { name: '个人资料' }, component: () => import('../views/admin/Person.vue') },
        { path: 'password', meta: { name: '修改密码' }, component: () => import('../views/admin/Password.vue') },
        { path: 'user', meta: { name: '用户信息' }, component: () => import('../views/admin/User.vue') },
      ]
    },
    {
      path: '/front',
      component: () => import('../views/Front.vue'),
      children: [
        { path: 'home', meta: { name: '系统首页' }, component: () => import('../views/user/Home.vue') },
        { path: 'publicity', meta: { name: '反诈宣传' }, component: () => import('../views/user/Publicity.vue') },
        { path: 'publicityDetail', meta: { name: '反诈宣传详情' }, component: () => import('../views/user/PublicityDetail.vue') },
        { path: 'video', meta: { name: '反诈视频' }, component: () => import('../views/user/Video.vue') },
        { path: 'videoDetail', meta: { name: '反诈视频详情' }, component: () => import('../views/user/VideoDetail.vue') },
        { path: 'activity', meta: { name: '反诈活动' }, component: () => import('../views/user/Activity.vue') },
        { path: 'activityDetail', meta: { name: '反诈活动详情' }, component: () => import('../views/user/ActivityDetail.vue') },
        { path: 'article', meta: { name: '反诈论坛' }, component: () => import('../views/user/Article.vue') },
        { path: 'articleDetail', meta: { name: '反诈论坛详情' }, component: () => import('../views/user/ArticleDetail.vue') },
        { path: 'report', meta: { name: '反诈举报' }, component: () => import('../views/user/Report.vue') },
        { path: 'notice', meta: { name: '系统公告' }, component: () => import('../views/user/Notice.vue') },
        { path: 'person', meta: { name: '个人中心' }, component: () => import('../views/user/Person.vue') },
        { path: 'password', meta: { name: '修改密码' }, component: () => import('../views/user/Password.vue') },
        { path: 'myActivitySignUp', meta: { name: '我的报名' }, component: () => import('../views/user/MyActivitySignUp.vue') },
        { path: 'myArticle', meta: { name: '我的帖子' }, component: () => import('../views/user/MyArticle.vue') },
        { path: 'myLikes', meta: { name: '我的点赞' }, component: () => import('../views/user/MyLikes.vue') },
        { path: 'myCollect', meta: { name: '我的收藏' }, component: () => import('../views/user/MyCollect.vue') },
        { path: 'myComment', meta: { name: '我的评论' }, component: () => import('../views/user/MyComment.vue') },
        { path: 'myReport', meta: { name: '我的举报' }, component: () => import('../views/user/MyReport.vue') },
        { path: 'myMessage', meta: { name: '我的消息' }, component: () => import('../views/user/MyMessage.vue') },
        { path: 'myArticleReport', meta: { name: '我的帖子举报' }, component: () => import('../views/user/MyArticleReport.vue') },
      ]
    },
    { path: '/login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
    { path: '/register', meta: { name: '注册' }, component: () => import('../views/Register.vue') },
    { path: '/404', meta: { name: '404' }, component: () => import('../views/404.vue') },
    { path: '/:pathMatch(.*)', redirect: '/404' }
  ]
})

export default router
