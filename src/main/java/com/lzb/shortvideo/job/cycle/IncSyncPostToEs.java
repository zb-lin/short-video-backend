//package com.lzb.shortvideo.job.cycle;

//import com.lzb.shortvideo.model.dto.post.PostEsDTO;

///**
// * 增量同步帖子到 es
// */
//// todo 取消注释开启任务
////@Component
//@Slf4j
//public class IncSyncPostToEs {
//
//    @Resource
//    private PostMapper postMapper;
//
//    @Resource
//    private PostEsDao postEsDao;
//
//    /**
//     * 每分钟执行一次
//     */
//    @Scheduled(fixedRate = 60 * 1000)
//    public void run() {
//        // 查询近 5 分钟内的数据
//        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
//        List<Post> postList = postMapper.listPostWithDelete(fiveMinutesAgoDate);
//        if (CollectionUtils.isEmpty(postList)) {
//            log.info("no inc post");
//            return;
//        }
//        List<PostEsDTO> postEsDTOList = postList.stream()
//                .map(PostEsDTO::objToDto)
//                .collect(Collectors.toList());
//        final int pageSize = 500;
//        int total = postEsDTOList.size();
//        log.info("IncSyncPostToEs start, total {}", total);
//        for (int i = 0; i < total; i += pageSize) {
//            int end = Math.min(i + pageSize, total);
//            log.info("sync from {} to {}", i, end);
//            postEsDao.saveAll(postEsDTOList.subList(i, end));
//        }
//        log.info("IncSyncPostToEs end, total {}", total);
//    }
//}
