-- 创建库
create database if not exists short_video;

-- 切换库
use short_video;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512) comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/up/admin/ban',
    video        varchar(1024) comment '发布视频列表（json 数组）',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;

-- 视频表
create table if not exists video
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint                             not null comment '创建用户 id',
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    url        varchar(512)                       not null comment '视频url',
    tags       varchar(1024)                      not null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '视频' collate = utf8mb4_unicode_ci;

-- 视频点赞表（硬删除）
create table if not exists video_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    videoId    bigint                             not null comment '视频 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (videoId),
    index idx_userId (userId)
) comment '视频点赞';

-- 视频收藏表（硬删除）
create table if not exists video_favour
(
    id         bigint auto_increment comment 'id' primary key,
    videoId    bigint                             not null comment '视频 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (videoId),
    index idx_userId (userId)
) comment '视频收藏';

-- 评论表
create table if not exists comment
(
    id         bigint auto_increment comment 'id' primary key,
    content    text                               null comment '内容',
    thumbNum   int      default 0                 not null comment '点赞数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '评论' collate = utf8mb4_unicode_ci;

-- 评论点赞表（硬删除）
create table if not exists comment_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    commentId  bigint                             not null comment '评论 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (commentId),
    index idx_userId (userId)
) comment '评论点赞';


create table if not exists `sensitive_word`
(
    `word` varchar(255) collate utf8mb4_unicode_ci not null comment '敏感词'
) engine = innodb
  default charset = utf8mb4
  collate = utf8mb4_unicode_ci comment ='敏感词库';
insert into `sensitive_word` (`word`)
values ('tmd');
insert into `sensitive_word` (`word`)
values ('fuck');